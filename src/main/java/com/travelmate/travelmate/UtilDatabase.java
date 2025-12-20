package com.travelmate.travelmate;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.model.*;

import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class UtilDatabase {


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        FirebaseService.initialize();
        Scanner sc = new Scanner(System.in);
        System.out.println("Welcome to Edit Center!");
        int choice = 0;
        while (choice != 4){
            System.out.println("1- Add City");
            System.out.println("2- Add Hobby");
            System.out.println("3- Add TripType");
            System.out.println("4- Exit");
            System.out.print("Enter your choice: ");
            choice = sc.nextInt();
            switch (choice){
                case 1:
                    //addCity();
                    break;
                case 2:
                    //addHobby();
                    break;
                case 3:
                    //addTripType();
                    break;
                    case 4:
                        System.out.println("Goodbye!");
                        break;
                    default:
                    System.out.println("Invalid choice!");
                    break;
            }
        }

    }



}
