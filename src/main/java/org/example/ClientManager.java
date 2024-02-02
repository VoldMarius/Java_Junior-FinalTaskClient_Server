package org.example;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientManager implements Runnable {


    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String name;

    public static ArrayList<ClientManager> clients = new ArrayList<>();

    public ClientManager(Socket socket) {
        try {
            this.socket = socket;
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            name = bufferedReader.readLine();
            clients.add(this);
            System.out.println(name + "joined the chat.");
            broadcastMessage("Server: " + name + "joined the chat.");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    // Удаление ПОЛЬЗОВАТЕЛЯ

    private void removeClient() {
        clients.remove(this);
        System.out.println(name + " left the chat.");
        broadcastMessage("Server: " + name + " left the chat.");
    }

    @Override
    public void run() {
        String massageFromClient;

        while (socket.isConnected()) {
            try {
                // Чтение данных
                massageFromClient = bufferedReader.readLine();
                if (massageFromClient == null) {
                    // для  macOS
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
                }

                String[] massageFromClientArray = massageFromClient.split(" ", 3);

                if(massageFromClientArray[1].charAt(0) == '@'){
                    // Отправка личных данных
                    String name = massageFromClientArray[1].substring( 1);
                    String massage = massageFromClientArray[0] + " " + massageFromClientArray[2];
                    System.out.println("name:" + name);
                    System.out.println("massage" + massage);
                    privateMessage(massage, name);
                } else {
                    // Отправка данных всем слушателям
                    broadcastMessage(massageFromClient);
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }

    }


   // Отправка личного сообщения

    private void privateMessage(String message, String findName) {
        for (ClientManager client : clients) {
            try {
                if (client.name.equals(findName) && message != null) {
                    client.bufferedWriter.write(message);
                    client.bufferedWriter.newLine();
                    client.bufferedWriter.flush();
                }
            }
            catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

     // Отправка сообщения всем слушателям

    private void broadcastMessage(String message) {
        for (ClientManager client : clients) {
            try {
                if (!client.name.equals(name) && message != null) {
                    client.bufferedWriter.write(message);
                    client.bufferedWriter.newLine();
                    client.bufferedWriter.flush();
                }
            }
            catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        // Удаление клиента из коллекции
        removeClient();
        try {
            // Завершаем работу буфера на чтение данных
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            // Завершаем работу буфера для записи данных
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            // Закрытие соединения с клиентским сокетом
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}