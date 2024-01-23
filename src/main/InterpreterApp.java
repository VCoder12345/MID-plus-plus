package main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import ast.AST;
import compiler.Compiler;
import parsing.Lexer;
import parsing.Parser;
import scope.ScopeDefinition;
import scope.ScopeResolution;
import vm.VM;

//a class for the actual interpreter-app
//runs source code, can be configured
public class InterpreterApp {
	public String sourcePath; //the path of the source code
	public boolean benchmarking = false; //should the interpreter print out the compiliation and execution-time
	public boolean traceExecution = false; //should the virtual machine trace the execution?
	public int mode = 0; //what the interpreter should do with the source-code: 0 --> execute, 1 --> print byte-code
	
	public InterpreterApp(String[] args) {
		parseArgs(args);
	}
	
	//parses the arguments that are given to the program
	public void parseArgs(String[] args) {
		int p = 0;
		while(p < args.length) {
			String arg = args[p];
			
			if(arg.charAt(0) == '-') {
				//option
				switch(arg.substring(1)) {
				case "b":
					//benchmarking
					int x = Integer.parseInt(args[++p]);
					benchmarking = x > 0;
					break;
				case "t":
					//trace execution
					x = Integer.parseInt(args[++p]);
					traceExecution = x > 0;
					break;
				}
			}else {
				//source-path
				sourcePath = arg;
			}
			
			++p;
		}
	}
	
	//compiles and executes the program
	public void exec() throws IOException {
		String sourceDir = sourcePath.substring(0, sourcePath.lastIndexOf("/") + 1 );

		String inputText = Files.readString(Path.of(sourcePath));
		String fileName = sourcePath.substring(sourcePath.lastIndexOf("/") + 1);
		
		long compilationStart = System.currentTimeMillis();
		Lexer lexer = new Lexer(inputText, fileName);
		Parser parser = new Parser(lexer, sourceDir);
		AST tree = parser.parse();
		
		ScopeDefinition scopeDef = new ScopeDefinition();
		scopeDef.build(tree);
		
		ScopeResolution scopeResolution = new ScopeResolution();
		scopeResolution.build(tree);
		Compiler compiler = new Compiler(scopeDef.currentScope.getNumVariables());
		compiler.compile(tree);
		if(benchmarking) {
			long compilationTime = System.currentTimeMillis() - compilationStart;
			double compilationSeconds = compilationTime / 1000.0;
			System.out.println("[*] compilation finished in " + compilationSeconds + " seconds");
		}
		
		
		VM vm = new VM(compiler.code);
		vm.traceExecution = traceExecution;
		
		if(benchmarking) {
			System.out.println("[*] starting program...");
			System.out.println();
		}
		
		long execStart = System.currentTimeMillis();
		vm.cpu();
		if(benchmarking) {
			long execTime = System.currentTimeMillis() - execStart;
			double execTimeSeconds = execTime / 1000.0;
			
			System.out.println();
			System.out.println("[*] execution finished in " + execTimeSeconds + " seconds");
		}
		
	}
	
	public static void main(String[] args) throws IOException {
		if(args.length > 0) {
			InterpreterApp app = new InterpreterApp(args);
			try {
				app.exec();
			}catch(RuntimeException ex) {
				//print the error-type (parsing-error, compiler-error, etc.)
				System.err.println(ex.getClass().getSimpleName());
				//ex.printStackTrace();
			} catch (IOException e) {
				System.err.println("Could not open the file at " + app.sourcePath);
			}
		}
	}
}
