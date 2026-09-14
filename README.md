# MID++

A dynamically typed programming language and bytecode interpreter written in Java. The project implements a complete language pipeline from source code to execution, including lexical analysis, parsing, compilation to custom bytecode, and a stack-based virtual machine.

## Features

* **Dynamic typing:** Values are typed and checked at runtime.
* **Functions:** User-defined functions with parameters, return values, and recursion.
* **Control flow:** Conditional branches and `while` loops.
* **Expressions:** Arithmetic, comparisons, boolean values, strings, and function calls.
* **Custom bytecode:** Source programs are compiled into a custom bytecode instruction set.
* **Stack-based VM:** Bytecode is executed by a stack-based virtual machine.
* **Console I/O:** Built-in input and output operations.

## Architecture

MID++ follows a complete language implementation pipeline:

```text
Source Code
    ↓
Lexer
    ↓
Parser
    ↓
Compiler
    ↓
Custom Bytecode
    ↓
Stack-based Virtual Machine
```

The lexer tokenizes source code, the parser constructs an internal representation, and the compiler translates it into bytecode instructions. The virtual machine then executes the bytecode using a stack-based execution model.

## Example

A recursive factorial function:

```text
out factorial((num)(in "Zahl: "))

fun factorial(x)
    if x <= 1
        return 1
    end

    return x * factorial(x - 1)
end
```

MID++ can also express larger programs with nested loops and control flow:

```text
var i = 10
var b = 10000

out "---prime numbers from " + i + " to " + b + "---"

while i <= b
    var c = 2
    var prime = true

    while c < i
        if i % c == 0
            c = i
            prime = false
        end
        c = c + 1
    end

    if prime
        out i
    end

    i = i + 1
end
```

## Background

MID++ evolved from **MID**, a programming language originally developed as part of my Matura thesis. The project continued the language and runtime implementation as a larger exploration of programming language design and virtual-machine architecture.
