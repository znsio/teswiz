#!/bin/bash

# Function to add two numbers
add() {
  echo "Enter two numbers to add:"
  read -p "First number: " num1
  read -p "Second number: " num2
  result=$(echo "$num1 + $num2" | bc)
  echo "Result: $result"
}

# Function to subtract two numbers
subtract() {
  echo "Enter two numbers to subtract:"
  read -p "First number: " num1
  read -p "Second number: " num2
  result=$(echo "$num1 - $num2" | bc)
  echo "Result: $result"
}

# Main function for the interactive menu
while true; do
  echo "--------------------"
  echo "Command Line Calculator"
  echo "Choose an operation:"
  echo "1. Add"
  echo "2. Subtract"
  echo "3. Exit"
  echo "--------------------"
  read -p "Select an option (1/2/3): " choice

  case $choice in
    1)
      add
      ;;
    2)
      subtract
      ;;
    3)
      echo "Exiting the calculator. Goodbye!"
      exit 0
      ;;
    *)
      echo "Invalid option, please try again."
      ;;
  esac
done
