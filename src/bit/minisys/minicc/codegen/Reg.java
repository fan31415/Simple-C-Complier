package bit.minisys.minicc.codegen;

public class Reg {
	public Reg(RegType regType, int regNum) {
		super();
		this.regType = regType;
		this.regNum = regNum;
	}
	private RegType regType;
	private int regNum;
	public RegType getRegType() {
		return regType;
	}
	public void setRegType(RegType regType) {
		this.regType = regType;
	}
	public int getRegNum() {
		return regNum;
	}
	public void setRegNum(int regNum) {
		this.regNum = regNum;
	}
	public String toString() {
		switch(regType) {
		case A:
			return "$a" + regNum;
		case T:
			return "$t" + regNum;
		case V:
			return "$v" + regNum;
		default:
			return "unknow";
		}
	}
	
}
