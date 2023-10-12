#! /usr/bin/env python3

def main():
    print("start")
    for x in range(4, 17):
        f = open("register_use_" + str(x) + ".deca", "w")
        f.write("{\n")
        for k in range(x):
            f.write("   int var_" + str(k) + " = " + str(k) + ";\n")
        f.write("}\n")
        f.close()


if __name__ == "__main__":
    main()