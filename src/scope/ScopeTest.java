package scope;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import ast.AST;
import parsing.Lexer;
import parsing.Parser;

public class ScopeTest {

	public static void main(String[] args) throws IOException {
		String inputText = Files.readString(Path.of("files/working"));
		
		//long compilationStart = System.currentTimeMillis();
		Lexer lexer = new Lexer(inputText);
		Parser parser = new Parser(lexer);
		AST tree = parser.parse();
		
		ScopeDefinition scopeDef = new ScopeDefinition();
		scopeDef.build(tree);
		
		ScopeResolution scopeResolution = new ScopeResolution();
		scopeResolution.build(tree);
	}

}
