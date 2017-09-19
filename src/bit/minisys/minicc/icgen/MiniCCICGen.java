package bit.minisys.minicc.icgen;

import java.io.File;
import java.io.IOException;

public class MiniCCICGen implements IMiniCCICGen {

	@Override
	public void run(String iFile, String oFile) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("iccodegen start");
		renameFile(iFile, oFile);
		System.out.println(iFile+ " " + oFile);
		System.out.println("iccodegen finish");
	}
	 public void renameFile(String oldname,String newname) { 
	        if(!oldname.equals(newname)){//新的文件名和以前文件名不同时,才有必要进行重命名 
	            File oldfile=new File(oldname); 
	            File newfile=new File(newname); 
	            if(!oldfile.exists()){
	            	System.out.println(oldname+"file not exist！"); 
	                return;//重命名文件不存在
	            }
	            if(newfile.exists())//若在该目录下已经有一个文件和新文件名相同，则不允许重命名 
	                System.out.println(newname+"已经存在！"); 
	            else{ 
	                oldfile.renameTo(newfile); 
	            } 
	        }else{
	            System.out.println("新文件名和旧文件名相同...");
	        }
	    }

}
