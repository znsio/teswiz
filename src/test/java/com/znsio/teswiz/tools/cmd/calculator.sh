#!/bin/bash

# Function to display the menu
function show_menu {
    echo "----------------------------------------------------"
    echo "Welcome to the Simple Command Line Calculator"
    echo "Please choose an option:"
    echo "1. Add"
    echo "2. Subtract"
    echo "3. Exit"
    echo "4. Log Errors"
    echo "----------------------------------------------------"
}

# Function to log errors in red to stderr
function log_error {
    echo -e "\033[31mError occurred: $1\033[0m" >&2
}

# Function to log results in green to stdout
function log_result {
    echo -e "\033[32mResult: $1\033[0m"
}

# Set log_errors to true permanently
log_errors=true

# Function to add two numbers
function add {
    echo -e "\nAddition:"
    echo -e "\nEnter first number:"
    read num1
    echo -e "\nEnter second number:"
    read num2
    sum=$(echo "$num1 + $num2" | bc)
    log_result "$sum"
}

# Function to subtract two numbers
function subtract {
    echo -e "\nSubtraction:"
    echo -e "\nEnter first number:"
    read num1
    echo -e "\nEnter second number:"
    read num2
    diff=$(echo "$num1 - $num2" | bc)
    log_result "$diff"
}

# Main loop to handle the user input
while true; do
    show_menu
    read choice
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
        4)
            echo "Logging error to stderr in red."
            log_error "Sample error: This is a test error message."
            ;;
        *)
            echo "Invalid option. Please try again."
            ;;
    esac
done
