#! /usr/bin/env python3

def main():
    inputs = ["decac",
        "decac -b",
        "decac -b ""$DECAC_HOME""/src/test/deca/compile/valid/hello_world.deca",
        "decac -p -v",
        "decac -r 25 ""$DECAC_HOME""/src/test/deca/compile/valid/hello_world.deca",
        "decac -r ""$DECAC_HOME""/src/test/deca/compile/valid/hello_world.deca 5",
        "decac -P ""$DECAC_HOME""/src/test/deca/compile/invalid/parallel_fail.deca",
        "decac -r 5 ""$DECAC_HOME""/src/test/deca/compile/valid/hello_world.deca",
        "decac -d ""$DECAC_HOME""/src/test/deca/compile/invalid/parallel_fail.deca",
        "decac -d -d ""$DECAC_HOME""/src/test/deca/compile/valid/hello_world.deca",
        "decac -d -d -d ""$DECAC_HOME""/src/test/deca/compile/valid/hello_world.deca",
        "decac -d -d -d -d ""$DECAC_HOME""/src/test/deca/compile/valid/hello_world.deca"]

    theorical_outputs=["decac [[-p | -v] [-n] [-r X] [-d]* [-P] [-w] <fichier deca>...] | [-b]",
        "gl20",
        "Impossible to call decac -b with other arguments",
        "Impossible to use -p and -v simultaneously",
        "Impossible to use more than 16 registers",
        "Invalid position of X in -r X option",
        "TODO parallèle fail",
        "TODO registers 5",
        "TODO d1",
        "TODO d2",
        "TODO d3",
        "TODO d4"]
    
    for k, input in enumerate(inputs):
        print("output=$(" + input + ")")
        print("expected_output=\"" + theorical_outputs[k] + "\"")
        print("((t = t + 1))")
        print("if [ \"$output\" = \"$expected_output\" ]; then")
        print(" echo -e \"${GREEN}CORRECT EXECUTIONS${NC}\"")
        print("else")
        print(" ((f = f + 1))")
        print(" echo -e \"${RED} Expected output: ${NC} ${expected_output} \"")
        print(" echo -e \"${RED} Output: ${NC} ${output} \"")
        print("fi")
        print("\n")

if __name__ == "__main__":
    main()