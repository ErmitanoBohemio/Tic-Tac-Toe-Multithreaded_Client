/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Formatter;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author OscarFabianHP
 */
//client side of client/server Tic-Tic-Toe program

//Each TicTacToeClient application mantains its own GUI version of the Tic-Tac-Toe board on which it displays the state 
//of the game. The client can place a mark only in a empty square.  
//class TicTacToeClient implements interface Runnable so that separate thread can read messages fom the server. this approach enables
//the user to interact with the board (in the event-dispatch thread) while waiting for messages from the server

public class TicTacToeClient extends JFrame implements Runnable{

    private JTextField idField; //textfield to display player's mark
    private JTextArea displayArea; //JTextArea to display output
    private JPanel boardPanel; //panel for tic-tac-toe board
    private JPanel panel2; //panel to hold board
    private Square[][] board; //tic-tac-toe board
    private Square currentSquare; //current square
    private Socket connection; //connection to server
    private Scanner input; //input from server
    private Formatter output; //output to server
    private String ticTacToeHost; //host name for server
    private String myMark; //this client's mark
    private boolean myTurn; //determines which client's turn it is
    private final String X_MARK = "X"; // mark for first client
    private final String O_MARK = "O"; //mark for second client
    
    //Agregado para ejercicio 28.20 
    private JPanel buttonsPanel;
    private JButton playAgainButton;
    private JButton terminateGameButton; 
    private boolean gameOver;
    private boolean salir;
    
    //set up user-interface and board
    public TicTacToeClient(String host){
        ticTacToeHost = host; //set name of server
        displayArea = new JTextArea(4, 30); //set up JTextArea
        displayArea.setEditable(false);
        //add(new JScrollPane(displayArea), BorderLayout.SOUTH);
        
        boardPanel = new JPanel(); //set up panel for squares in board
        boardPanel.setLayout(new GridLayout(3, 3, 0, 0));
        
        board = new Square[3][3]; //create board
        
        //loop over the rows in the board
        for (int row = 0; row < board.length; row++) {
            //loop over the columns in the board
            for (int column = 0; column < board[row].length; column++) {
                //create square
                board[row][column] = new Square(" ", row * 3 + column); //la formula me permite obtener (0, 1, 2, 3, 4, 5, 6, 7, 8)
                boardPanel.add(board[row][column]); //add square
            }
        }
        
        idField = new JTextField(); //set  up textfield
        idField.setEditable(false);
        add(idField, BorderLayout.NORTH);
        
        panel2 = new JPanel(); //set up panel to contain boardPanel
        panel2.add(boardPanel, BorderLayout.CENTER); //add board panel
        add(panel2, BorderLayout.CENTER); //add container panel
        
        
        //buttonsPanel = new JPanel();
        //buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        gameOver=false;
        salir=false;
        
        Box southBox = Box.createVerticalBox();
        playAgainButton = new JButton("Play again");
        playAgainButton.setVisible(false);
        playAgainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendClickedSquare(9); //se envia 9 para que el servidor lo interprete como volver a jugar
            }
        });
        
        terminateGameButton = new JButton("Terminate Game");
        terminateGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendClickedSquare(10);
            }
        });
        Box buttonBox = Box.createHorizontalBox();
        
        southBox.add(new JScrollPane(displayArea));
        buttonBox.add(terminateGameButton);
        buttonBox.add(playAgainButton);
        southBox.add(buttonBox);
        add(southBox, BorderLayout.SOUTH);
        //buttonsPanel.add(new JScrollPane(displayArea));
        //buttonsPanel.add(terminateGameButton);
        //buttonsPanel.add(playAgainButton);
        //add(buttonsPanel, BorderLayout.SOUTH);
        
        //setSize(300, 225); //set size window

        setSize(300, 270); //set size window

        setVisible(true); //show window
        
        startClient();
    }
    
    //start the client thread
    //the starClient method opens a connection to the server and gets the associated input and output streams from the socket object.
    public void startClient() {
        try{ //connect to server and get stream
            connection = new Socket(InetAddress.getByName(ticTacToeHost), 12345);
            
            //get streams for input and output
            input = new Scanner(connection.getInputStream());
            output = new Formatter(connection.getOutputStream());
            
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        
        //create and start worker thread for this client
        ExecutorService worker = Executors.newFixedThreadPool(1);
        worker.execute(this); //execute client
    }
    
    //control thread that allows continuous update of displayArea
    //run method controls the separate thread of execution. the method first reads the mark character (X or O) from the server
    //the loops continuously and reads messages from the server, each message is passed to the processMessage method for processing.
    @Override
    public void run() {
        try {
            myMark = input.nextLine(); //get Player's mark (X or O)
            
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    //display Player's mark
                    idField.setText("You are player \"" + myMark + "\"" );
                }
            });
            
            myTurn = (myMark.equals(X_MARK)); //determine if client's turn
            
            //receive messages sent to client and output them
            //while(true){
            while(true){
                if(input.hasNextLine()){
                    processMessage(input.nextLine());
                    if(salir)
                        break;
                }
            }    
            System.out.println("Salir");
            input.close();
            output.close();
            connection.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //process message received  by client
    private void processMessage(String message) {
        
        //valid move occurred
        if(message.equals("Valid move.")){ 
            displayMessage("Valid move, please wait.\n");
            setMark(currentSquare, myMark); //set mark in square (the one in which user clicked)
        }
        else if(message.equals("Invalid move, try again")) {
            displayMessage(message + "\n"); //display invalid move
            myTurn  = true;
        }
        else if(message.equals("Opponent moved"))
        {
            int location = input.nextInt(); //get move location, reads an integer from the server indicating where the opponent moved
            input.nextLine();
            int row = location / 3; //calculate row
            int column = location % 3; //calculate column
            
            setMark(board[row][column],  //and place a mark in that square of the board
                    (myMark.equals(X_MARK) ? O_MARK : X_MARK)); //mark move
            displayMessage("Opponent moved. Your turn.\n");
            myTurn = true; //now this client's turn
        }
        else if(message.equals("neither won") || message.equals("Winner Player X") || message.equals("Winner Player O")){
            displayMessage(message + "\n");
            playAgainButton.setVisible(true);
            terminateGameButton.setEnabled(false);
            gameOver=true;
        }
        else if(message.equals("Lets play again")){
           playAgain(); //llamada a metodo para reinciar juego
           displayMessage("New game is about to begin so that its board and state was reset\n");
           displayMessage(message +"\n");
            displayMessage("X Player begin\n");
        }
        else if(message.equals("Terminated game by player " + myMark +"\n")){
                salir=true;
                displayMessage(message+"\n");
        }
        else //if any other message is received simply displays the message
            displayMessage(message + "\n");
        }

    //manipulate displayArea in event-dispatch thread
    private void displayMessage(final String messageToDisplay) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                displayArea.append(messageToDisplay); //updates output
            }
        });
    }

    //utility method to set mark on board in event-dispatch thread
    private void setMark(final Square squareToMark, final String mark) {
        SwingUtilities.invokeLater(new Runnable() { //using SwingUtilities  method invokeLater to ensure that the GUI updates occur in the event-dispatch thread
            @Override
            public void run() {
                squareToMark.setMark(mark); //set mark in square
            }
        });
    }

    //send message to server indicating clicked square
    public void sendClickedSquare(int location){
        System.out.println(myTurn+" "+myMark);
        
        //Agregado para ejercicio 28.20 punto B
        if(location==9){
            output.format("%d\n", location); //send location to server
            output.flush();
        }
        //Agregado para ejercicio 28.20 punto C
        else if(location==10){
            output.format("%d\n", location); //send location to server
            output.flush();
        }
        //if it is my turn
        else if(myTurn)
        {
            output.format("%d\n", location); //send location to server
            output.flush();
            myTurn = false; //not my turn any more
        }
    }
    
    // set current Square
    public void setCurrentSquare(Square square){
        currentSquare = square; //set current square to argument
    }

    //metodo para implementar ejercico 28.20 
    //metodo utilitario para reiniciat tablero e informacion de estado
    private void playAgain(){
        //reinicia turno de jugadores primero mueve X y luego O
        if(myMark.equals(X_MARK)) //determine if client's turn)
            myTurn=true;
        else
            myTurn=false;
        
        terminateGameButton.setEnabled(true);
        playAgainButton.setVisible(false);
        gameOver = false;
        //reinicia el tablero Tic-Tac-Toe
        for(Square[] squareRow:board)
            for(Square square:squareRow )
                square.setMark(" ");
    }
    
    //private inner class for the squares on the board
    //the inner class Square implements each of the nine squares on the board     
    private class Square extends JPanel{

        private String mark; //mark to be drawn in this square
        private int location; //location of square
        
        public Square(String squareMark, int squareLocation) {
            mark = squareMark; //set mark for this square
            location = squareLocation; //set location of this square
            
            
            addMouseListener(new MouseAdapter() {
                public void mouseReleased(MouseEvent e){
                    if(!gameOver){
                        setCurrentSquare(Square.this); //set current square

                        //send location of this square
                        sendClickedSquare(getSquareLocation());
                    }
                }

                
            });
        }

        //return preferred size of Square
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(30, 30); //return preferred size
        }

        //return minimum size of Square
        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize(); //return preferred size
        }

        //set mark of Square 
        public void setMark(String newMark) {
            mark = newMark; //set mark of square 
            repaint(); //repaint square
        }
        
        //return Square location
        public int getSquareLocation() {
            return location; //return location of square
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
            
            g.drawRect(0, 0, 29, 29); //draw square
            g.drawString(mark, 11, 20); //draw mark
        }
        
        
    }
    
}
