/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

import javax.swing.JFrame;

/**
 *
 * @author OscarFabianHP
 */
public class TicTacToeClientTest {
    public static void main(String[] args) {
        TicTacToeClient application; //declare client application
        
        if(args.length == 0)
            application = new TicTacToeClient("127.0.0.1"); //localhost
        else
            application = new TicTacToeClient(args[0]); //use args
        
        application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
    }
    
}
