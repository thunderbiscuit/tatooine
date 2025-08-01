#!/usr/bin/env python3

import re
from datetime import datetime
from collections import defaultdict

def parse_log_file(log_file_path):
    """Parse the log file and extract transaction data and wallet balances."""
    transactions_per_month = defaultdict(int)
    final_balance = None

    with open(log_file_path, 'r') as file:
        for line in file:
            line = line.strip()

            # Look for successful coin sends
            if "Faucet sent coins" in line:
                # Extract date from the beginning of the line
                date_match = re.match(r'^(\d{4}-\d{2}-\d{2})', line)
                if date_match:
                    date_str = date_match.group(1)
                    date_obj = datetime.strptime(date_str, '%Y-%m-%d')
                    month_key = date_obj.strftime('%Y-%m')
                    transactions_per_month[month_key] += 1

            # Look for wallet balance updates
            elif "Wallet synced. Balance:" in line:
                # Extract the balance
                balance_match = re.search(r'balance: (\d+)', line)
                if balance_match:
                    final_balance = int(balance_match.group(1))

    return transactions_per_month, final_balance

def main():
    log_file_path = input("Enter the path to your log file: ").strip()

    try:
        transactions_per_month, final_balance = parse_log_file(log_file_path)

        print("\nFaucet Statistics")
        print("-" * 30)

        if transactions_per_month:
            print("\nSuccessful faucet pulls per month")
            print("-" * 30)
            for month in sorted(transactions_per_month.keys()):
                print(f"{month}: {transactions_per_month[month]} transactions")

            total_transactions = sum(transactions_per_month.values())
            print(f"\nTotal transactions")
            print("-" * 30)
            print(f"\n{total_transactions}")
        else:
            print("No successful transactions found in the log file.")

        if final_balance is not None:
            print(f"\nFinal wallet balance")
            print("-" * 30)
            print(f"\n{final_balance} satoshis")
        else:
            print("\nNo wallet balance information found in the log file.")

    except FileNotFoundError:
        print(f"Error: Could not find the log file at '{log_file_path}'")
    except Exception as e:
        print(f"Error processing log file: {e}")

if __name__ == "__main__":
    main()
