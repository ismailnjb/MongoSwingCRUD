package org.example;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UserCRUDApp {
    private MongoCollection<Document> userCollection;
    private JFrame frame;
    private JTextField nameField;
    private JTextField emailField;
    private JTable table;
    private DefaultTableModel tableModel;

    public UserCRUDApp() {
        // Initialize MongoDB Collection
        MongoDatabase database = MongoDBUtil.getDatabase();
        userCollection = database.getCollection("users");

        // Setup Swing UI
        frame = new JFrame("User CRUD App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        // Panel for input fields
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(3, 2));
        inputPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        inputPanel.add(emailField);

        JButton addButton = new JButton("Add User");
        inputPanel.add(addButton);
        JButton updateButton = new JButton("Update User");
        inputPanel.add(updateButton);

        frame.add(inputPanel, BorderLayout.NORTH);

        // Table for displaying users
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Email"}, 0);
        table = new JTable(tableModel);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);

        // Panel for delete button
        JPanel buttonPanel = new JPanel();
        JButton deleteButton = new JButton("Delete User");
        buttonPanel.add(deleteButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // Add listeners
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addUser();
            }
        });

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateUser();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteUser();
            }
        });

        // Load users from MongoDB
        loadUsers();

        frame.setVisible(true);
    }

    private void addUser() {
        String name = nameField.getText();
        String email = emailField.getText();

        if (name.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Name and Email cannot be empty.");
            return;
        }

        Document newUser = new Document("name", name).append("email", email);
        userCollection.insertOne(newUser);

        nameField.setText("");
        emailField.setText("");
        loadUsers();
    }

    private void updateUser() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a user to update.");
            return;
        }

        String id = tableModel.getValueAt(selectedRow, 0).toString();
        String newName = nameField.getText();
        String newEmail = emailField.getText();

        if (newName.isEmpty() || newEmail.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Name and Email cannot be empty.");
            return;
        }

        Document updatedUser = new Document("$set", new Document("name", newName).append("email", newEmail));
        userCollection.updateOne(new Document("_id", new org.bson.types.ObjectId(id)), updatedUser);

        nameField.setText("");
        emailField.setText("");
        loadUsers();
    }

    private void deleteUser() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a user to delete.");
            return;
        }

        String id = tableModel.getValueAt(selectedRow, 0).toString();
        userCollection.deleteOne(new Document("_id", new org.bson.types.ObjectId(id)));

        loadUsers();
    }

    private void loadUsers() {
        tableModel.setRowCount(0);

        for (Document user : userCollection.find()) {
            tableModel.addRow(new Object[]{
                    user.getObjectId("_id").toString(),
                    user.getString("name"),
                    user.getString("email")
            });
        }
    }

    public static void main(String[] args) {
        new UserCRUDApp();
    }
}
