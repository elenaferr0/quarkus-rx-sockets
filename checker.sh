#!/bin/bash

# Usage: ./check_numbers.sh <max_number> <file>
MAX_NUMBER=$1
FILE=$2

# Generate the sequence of numbers from 0 to MAX_NUMBER
for i in $(seq 0 $MAX_NUMBER); do
    # Check if the number is present in the file
    if ! grep -q -w "$i" "$FILE"; then
        echo "Number $i is missing from the file."
    fi
done

echo "All numbers from 0 to $
