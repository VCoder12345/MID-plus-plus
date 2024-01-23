package parsing;

//a token
public class Token {
	public TokenType type; //the type of the token
	public String value; //the text of the token
	public int line; //the line in which the token was found
	public String file;
	
	public Token(TokenType type, String value, int line, String file) {
		super();
		this.type = type;
		this.value = value;
		this.line = line;
		this.file = file;
	}

	
	public Token(TokenType type, int line, String file) {
		this(type, "", line, file);
	}
	
	@Override
	public String toString() {
		return "Token [type=" + type + ", value= \"" + value + "\"]";
	}

	
	
	
	
}
