/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.JOptionPane;


/**
 *
 * @author WW
 */
public class Mysql {
    
    private static Connection c;
    private static final String userName="root";
    private static final String password="200301403251A";
    private static final String dataBase="oceanops";
    
    public static Statement createConnection()throws Exception{
        
        if(c==null){
            Class.forName("com.mysql.cj.jdbc.Driver");
              c=DriverManager.getConnection("jdbc:mysql://localhost:3306/"+dataBase,userName,password); 
        }
       
             Statement s=c.createStatement();
             return s;
    }
    public static void iud(String query){
        try {
             
             
             createConnection().executeUpdate(query);
        } catch (Exception e) {
            System.out.println("Error");
        }   
    }
   
    
    public static ResultSet search(String query)throws Exception{
         
             ResultSet rs=createConnection().executeQuery(query);
             
           return rs;
          
    }
}
