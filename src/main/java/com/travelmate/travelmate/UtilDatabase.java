package com.travelmate.travelmate;
import com.travelmate.travelmate.firebase.FirebaseService;
import com.travelmate.travelmate.model.*;

import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class UtilDatabase {

    public static void addCity() throws ExecutionException, InterruptedException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter City Name: ");
        String cityName = sc.nextLine();
        System.out.print("Enter Fun Point: ");
        int funPoint = sc.nextInt();
        System.out.print("Enter Culture Point: ");
        int culturePoint = sc.nextInt();
        System.out.print("Enter Chill Point: ");
        int chillPoint = sc.nextInt();
        City city = new City(cityName, cityName, "");
        int[] scores = new int[3];
        scores[0] = funPoint;
        scores[1] = culturePoint;
        scores[2] = chillPoint;
        city.setCompatibilityScores(scores);
        System.out.println("City Created!\nName: " + cityName + "\nFun Point: " + funPoint + "\nCulture Point: " + culturePoint + "\nChill Point: " + chillPoint + "\n");
    }

    public static void addHobby() throws ExecutionException, InterruptedException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter Hobby Name: ");
        String hobbyName = sc.nextLine();
        System.out.print("Enter Fun Point: ");
        int funPoint = sc.nextInt();
        System.out.print("Enter Culture Point: ");
        int culturePoint = sc.nextInt();
        System.out.print("Enter Chill Point: ");
        int chillPoint = sc.nextInt();
        Hobby hobby = new Hobby(hobbyName, hobbyName);
        int[] scores = new int[3];
        scores[0] = funPoint;
        scores[1] = culturePoint;
        scores[2] = chillPoint;
        hobby.setCompatibilityScores(scores);
        System.out.println("Hobby Created!\nName: " + hobbyName + "\nFun Point: " + funPoint + "\nCulture Point: " + culturePoint + "\n" + "Chill Point: " + chillPoint + "\n");

    }

    public static void addTripType() throws ExecutionException, InterruptedException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter TripType Name: ");
        String tripTypeName = sc.nextLine();
        System.out.print("Enter Fun Point: ");
        int funPoint = sc.nextInt();
        System.out.print("Enter Culture Point: ");
        int culturePoint = sc.nextInt();
        System.out.print("Enter Chill Point: ");
        int chillPoint = sc.nextInt();
        TripTypes tripType = new TripTypes(tripTypeName, tripTypeName);
        int[] scores = new int[3];
        scores[0] = funPoint;
        scores[1] = culturePoint;
        scores[2] = chillPoint;
        tripType.setCompatibilityScores(scores);
        System.out.println("Hobby Created!\nName: " + tripTypeName + "\nFun Point: " + funPoint + "\nCulture Point: " + culturePoint + "\n" + "Chill Point: " + chillPoint + "\n");

    }

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
                    addCity();
                    break;
                case 2:
                    addHobby();
                    break;
                case 3:
                    addTripType();
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
