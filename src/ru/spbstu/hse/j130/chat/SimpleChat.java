/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.spbstu.hse.j130.chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author roman
 */
public class SimpleChat extends Thread implements ISimpleChat {

    private final Socket s;
    private String message;
    private final ObjectInputStream inputStream;
    private final ObjectOutputStream outputStream;

    public SimpleChat(int port) throws IOException {
        this.s = new Socket(InetAddress.getByName("localhost"), port);
        this.outputStream = new ObjectOutputStream(s.getOutputStream());
        this.inputStream = new ObjectInputStream(s.getInputStream());
    }

    public SimpleChat() throws IOException {
        ServerSocket ss = new ServerSocket(SERVER_PORT);
        this.s = ss.accept();
        this.inputStream = new ObjectInputStream(s.getInputStream());
        this.outputStream = new ObjectOutputStream(s.getOutputStream());
    }

    public static void main(String[] args) throws ChatException {
        Scanner sc = new Scanner(System.in);
        System.out.println("If server press 1, client 0");
        int server = sc.nextInt();

        if (server == 1) {
            while (true) {
                try (SimpleChat simpleChat = new SimpleChat()) {
                    simpleChat.server();
                } catch (IOException e) {
                    throw new ChatException("IOException: " + e.getMessage());
                }
            }
        } else {
            try (SimpleChat simpleChat = new SimpleChat(SERVER_PORT)) {
                simpleChat.client();
            } catch (IOException e) {
                throw new ChatException("IOException: " + e.getMessage());
            }
        }

        try {
            Thread.sleep(60000);
        } catch (InterruptedException ex) {
            throw new ChatException("InterruptedException: " + ex.getMessage());
        }
        System.out.println("SimpleChat finished.");
    }

    @Override
    public void client() {
        System.out.println("Client started");
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                }
                try {
                    this.getMessage();
                } catch (ChatException ex) {
                }
            }
        }).start();

        while (true) {
            try {
                Scanner sc = new Scanner(System.in);
                System.out.println("Enter a client message: ");
                message = sc.nextLine();
                this.sendMessage(message);
                if (message.equals("Stop")) {
                    System.out.println("Client stopped");
                    break;
                }
            } catch (ChatException ex) {
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
    }

    @Override
    public void server() {
        System.out.println("Server started");
        new Thread(() -> {
            while (true) {
                try {
                    this.getMessage();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                    }
                } catch (ChatException ex) {
                }
            }
        }).start();

        while (true) {
            try {
                Scanner sc = new Scanner(System.in);
                System.out.println("Enter a server message: ");
                message = sc.nextLine();
                this.sendMessage(message);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                }
            } catch (ChatException ex) {
            }
        }
    }

    @Override
    public String getMessage() throws ChatException {
        try {
            Object o = inputStream.readObject();
            message = o.toString();
            System.out.println("Response: " + message);
        } catch (IOException ex) {
            throw new ChatException("IOException: " + ex.getMessage());
        } catch (ClassNotFoundException ex) {
        }

        return message;
    }

    @Override
    public void sendMessage(String message) throws ChatException {
        try {
            outputStream.writeObject(message);
        } catch (IOException ex) {
            throw new ChatException("IOException: " + ex.getMessage());
        }
    }

    @Override
    public void close() throws ChatException {
        try {
            s.close();
        } catch (IOException ex) {
            throw new ChatException("IOException: " + ex.getMessage());
        }
    }

}
