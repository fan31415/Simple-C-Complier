package bit.minisys.minicc.scanner;

public class Token {
	private int number;
	private String value;
	private Type type;
	private int line;
    private	boolean isValid;
    private Mark mark;
    public Token() {
    	
    }
    public Token(int num, String val, Type type, int line, boolean isValid) {
    	this.number = num;
    	this.value = val;
    	this.type = type;
    	this.line = line;
    	this.isValid = isValid;
    }
    public Token(int num, String val, Type type, Mark mark, int line, boolean isValid) {
    	this.number = num;
    	this.value = val;
    	this.type = type;
    	this.mark = mark;
    	this.line = line;
    	this.isValid = isValid;
    }
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public int getLine() {
		return line;
	}
	public void setLine(int line) {
		this.line = line;
	}
	public boolean isValid() {
		return isValid;
	}
	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}
	public Mark getMark() {
		return mark;
	}
	public void setMark(Mark mark) {
		this.mark = mark;
	}
	
}
