package parsing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import ast.AST;
import ast.ASTPrinter;

//tests the parser by parsing a file, generating an AST and printing it
public class ParserTest {

	public static void main(String[] args) throws IOException {
		Lexer lexer = new Lexer(Files.readString(Path.of("files/test.txt")));
		Parser parser = new Parser(lexer);
		AST tree = parser.parse();
		ASTPrinter printer = new ASTPrinter();
		printer.print(tree);
	}

}
