#! /usr/bin/env python3

from random import randint

def main():
    print("{")
    nVar = 5000
    for i in range(nVar):
        print("int variable_" + str(i) + " = " + str(randint(0, 1000000)) + ";")
    print("}")

if __name__ == "__main__":
    main()