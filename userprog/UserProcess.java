package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;

import java.io.EOFException;

/**
 * Encapsulates the state of a user process that is not contained in its
 * user thread (or threads). This includes its address translation state, a
 * file table, and information about the program being executed.
 *
 * <p>
 * This class is extended by other classes to support additional functionality
 * (such as additional syscalls).
 *
 * @see	nachos.vm.VMProcess
 * @see	nachos.network.NetProcess
 */

public class UserProcess {
    /**
     * Allocate a new process.
     */
    public UserProcess() {
	
		int numPhysPages = Machine.processor().getNumPhysPages();
		pageTable = new TranslationEntry[numPhysPages];
		for (int i=0; i<numPhysPages; i++)
			pageTable[i] = new TranslationEntry(i,i, true,false,false,false);
			
		//create table large enough to handle up to 16 files at one time
		
		fdTable = new OpenFile[MAXFD]; //MAXFD = 16 
		
		//fileDescriptors 0 and 1 must refer to standard input and out (cant be null)
		fdTable[0] = UserKernel.console.openForReading(); //STDIN
		Lib.assertTrue(fdTable[0] != null);
		fdTable[1] = UserKernel.console.openForWriting(); //STDOUT
		Lib.assertTrue(fdTable[1] != null);

    }
    
    /**
     * Allocate and return a new process of the correct class. The class name
     * is specified by the <tt>nachos.conf</tt> key
     * <tt>Kernel.processClassName</tt>.
     *
     * @return	a new process of the correct class.
     */
    public static UserProcess newUserProcess() {
	return (UserProcess)Lib.constructObject(Machine.getProcessClassName());
    }

    /**
     * Execute the specified program with the specified arguments. Attempts to
     * load the program, and then forks a thread to run it.
     *
     * @param	name	the name of the file containing the executable.
     * @param	args	the arguments to pass to the executable.
     * @return	<tt>true</tt> if the program was successfully executed.
     */
    public boolean execute(String name, String[] args) {
	if (!load(name, args))
	    return false;
	
	new UThread(this).setName(name).fork();

	return true;
    }

    /**
     * Save the state of this process in preparation for a context switch.
     * Called by <tt>UThread.saveState()</tt>.
     */
    public void saveState() {
    }

    /**
     * Restore the state of this process after a context switch. Called by
     * <tt>UThread.restoreState()</tt>.
     */
    public void restoreState() {
	Machine.processor().setPageTable(pageTable);
    }

    /**
     * Read a null-terminated string from this process's virtual memory. Read
     * at most <tt>maxLength + 1</tt> bytes from the specified address, search
     * for the null terminator, and convert it to a <tt>java.lang.String</tt>,
     * without including the null terminator. If no null terminator is found,
     * returns <tt>null</tt>.
     *
     * @param	vaddr	the starting virtual address of the null-terminated
     *			string.
     * @param	maxLength	the maximum number of characters in the string,
     *				not including the null terminator.
     * @return	the string read, or <tt>null</tt> if no null terminator was
     *		found.
     */
    public String readVirtualMemoryString(int vaddr, int maxLength) {
	Lib.assertTrue(maxLength >= 0);

	byte[] bytes = new byte[maxLength+1];

	int bytesRead = readVirtualMemory(vaddr, bytes);

	for (int length=0; length<bytesRead; length++) {
	    if (bytes[length] == 0)
		return new String(bytes, 0, length);
	}

	return null;
    }

    /**
     * Transfer data from this process's virtual memory to all of the specified
     * array. Same as <tt>readVirtualMemory(vaddr, data, 0, data.length)</tt>.
     *
     * @param	vaddr	the first byte of virtual memory to read.
     * @param	data	the array where the data will be stored.
     * @return	the number of bytes successfully transferred.
     */
    public int readVirtualMemory(int vaddr, byte[] data) {
	return readVirtualMemory(vaddr, data, 0, data.length);
    }

    /**
     * Transfer data from this process's virtual memory to the specified array.
     * This method handles address translation details. This method must
     * <i>not</i> destroy the current process if an error occurs, but instead
     * should return the number of bytes successfully copied (or zero if no
     * data could be copied).
     *
     * @param	vaddr	the first byte of virtual memory to read.
     * @param	data	the array where the data will be stored.
     * @param	offset	the first byte to write in the array.
     * @param	length	the number of bytes to transfer from virtual memory to
     *			the array.
     * @return	the number of bytes successfully transferred.
     */
    public int readVirtualMemory(int vaddr, byte[] data, int offset,
				 int length) {
	Lib.assertTrue(offset >= 0 && length >= 0 && offset+length <= data.length);

	byte[] memory = Machine.processor().getMemory();
	int VPN1 = Processor.pageFromAddress(vaddr);
	int offset1 = Processor.offsetFromAddress(vaddr);
	int VPNend = Processor.pageFromAddress(vaddr + length);
	
	TranslationEntry entry = getTranslationEntry(VPN1, false);
	if (entry == null){
		return 0;
	}


	int amount = Math.min(length, pageSize - offset1);
	System.arraycopy(memory, Processor.makeAddress(entry.ppn, offset1), data, offset, amount);
	offset += amount;
	for (int i = VPN1 +1; i<= VPNend; i++){
		entry= getTranslationEntry(i, false);
		if (entry== null){
			return amount;
		}
		int length2 = Math.min(length-amount, pageSize);
		System.arraycopy(memory, Processor.makeAddress(entry.ppn, 0), data, offset, length2);
		offset += length2;
		amount += length2;
 	}
 	
 	return amount;
				 }
/**
	int VPN1 = Processor.pageFromAddress(vaddr);
	int offset1 = Processor.offsetFromAddress(vaddr);
	int amount = 0;
	
	for(int vpn = VPN1; length > 0; ++vpn) {
			int length2 = Math.min(length - amount, pageSize - offset1);
			TranslationEntry Entry = getEntry(vpn, false);
			if (Entry == null) {
				return amount;
			}
			System.arraycopy(memory, Processor.makeAddress(Entry.ppn, offset1), data, offset, length2);
			offset += length2;
			amount += length2;
			length -= length2;
			
			offset1 = 0;
	
	
	}
	
	return amount;
    }
**/
    /**
     * Transfer all data from the specified array to this process's virtual
     * memory.
     * Same as <tt>writeVirtualMemory(vaddr, data, 0, data.length)</tt>.
     *
     * @param	vaddr	the first byte of virtual memory to write.
     * @param	data	the array containing the data to transfer.
     * @return	the number of bytes successfully transferred.
     */
    public int writeVirtualMemory(int vaddr, byte[] data) {
	return writeVirtualMemory(vaddr, data, 0, data.length);
    }

    /**
     * Transfer data from the specified array to this process's virtual memory.
     * This method handles address translation details. This method must
     * <i>not</i> destroy the current process if an error occurs, but instead
     * should return the number of bytes successfully copied (or zero if no
     * data could be copied).
     *
     * @param	vaddr	the first byte of virtual memory to write.
     * @param	data	the array containing the data to transfer.
     * @param	offset	the first byte to transfer from the array.
     * @param	length	the number of bytes to transfer from the array to
     *			virtual memory.
     * @return	the number of bytes successfully transferred.
     */
    public int writeVirtualMemory(int vaddr, byte[] data, int offset,
				  int length) {
	Lib.assertTrue(offset >= 0 && length >= 0 && offset+length <= data.length);

	byte[] memory = Machine.processor().getMemory();
	
	int VPN1 = Processor.pageFromAddress(vaddr);
 	int offset1 = Processor.offsetFromAddress(vaddr);
	int VPNend = Processor.pageFromAddress(vaddr + length);
	
	TranslationEntry entry = getTranslationEntry(VPN1, true);
	if (entry == null){
		return 0;
	}
	int amount = Math.min(length, pageSize - offset1);
	System.arraycopy(data, offset, memory, Processor.makeAddress(entry.ppn, offset1), amount);
	offset += amount;

	for (int i = VPN1 +1; i<= VPNend; i++){
		entry= getTranslationEntry(i, true);
		if (entry== null){	
			return amount;
 		}
 		int length2 = Math.min(length-amount, pageSize);
		System.arraycopy(data, offset, memory, Processor.makeAddress(entry.ppn, 0), length2);
		offset += length2;
		amount += length2;
}
	return amount;
				  }
				  
	protected TranslationEntry getTranslationEntry(int virtual, boolean write) {
		if (virtual < 0 || virtual >= numPages)
			return null;
		TranslationEntry result = pageTable[virtual];
		if (result == null)
			return null;
		if (result.readOnly && write)
			return null;
		result.used = true;
		if (write)
			result.dirty = true;
		return result;
	}
/*
	int VPN1 = Processor.pageFromAddress(vaddr);
	int offset1 = Processor.offsetFromAddress(vaddr);
	int amount = 0;
	
	for (int vpn = VPN1; length > 0; ++vpn) {
		int length2 = Math.min(length - amount, pageSize - offset1);
		TranslationEntry Entry = getEntry(vpn, true);
		if (Entry == null) {
			return amount;
		}
		System.arraycopy(data, offset, memory, Processor.makeAddress(Entry.ppn, offset1), length2);
		offset += length2;
		amount += length2;
		length -= length2;
		offset1 = 0;
		}
	
	return amount;
    }

     public TranslationEntry getEntry(int virtual, boolean write) {
		if (virtual >= pageTable.length) {
			return null;
		}
		
		if (pageTable[virtual].readOnly && write) {
			return null;
		}
		
		pageTable[virtual].used = true;
		pageTable[virtual].dirty |= write;
		return pageTable[virtual];
	}
	*/
    /**
     * Load the executable with the specified name into this process, and
     * prepare to pass it the specified arguments. Opens the executable, reads
     * its header information, and copies sections and arguments into this
     * process's virtual memory.
     *
     * @param	name	the name of the file containing the executable.
     * @param	args	the arguments to pass to the executable.
     * @return	<tt>true</tt> if the executable was successfully loaded.
     */
    private boolean load(String name, String[] args) {
	Lib.debug(dbgProcess, "UserProcess.load(\"" + name + "\")");
	
	OpenFile executable = ThreadedKernel.fileSystem.open(name, false);
	if (executable == null) {
	    Lib.debug(dbgProcess, "\topen failed");
	    return false;
	}

	try {
	    coff = new Coff(executable);
	}
	catch (EOFException e) {
	    executable.close();
	    Lib.debug(dbgProcess, "\tcoff load failed");
	    return false;
	}

	// make sure the sections are contiguous and start at page 0
	numPages = 0;
	for (int s=0; s<coff.getNumSections(); s++) {
	    CoffSection section = coff.getSection(s);
	    if (section.getFirstVPN() != numPages) {
		coff.close();
		Lib.debug(dbgProcess, "\tfragmented executable");
		return false;
	    }
	    numPages += section.getLength();
	}

	// make sure the argv array will fit in one page
	byte[][] argv = new byte[args.length][];
	int argsSize = 0;
	for (int i=0; i<args.length; i++) {
	    argv[i] = args[i].getBytes();
	    // 4 bytes for argv[] pointer; then string plus one for null byte
	    argsSize += 4 + argv[i].length + 1;
	}
	if (argsSize > pageSize) {
	    coff.close();
	    Lib.debug(dbgProcess, "\targuments too long");
	    return false;
	}

	// program counter initially points at the program entry point
	initialPC = coff.getEntryPoint();	

	// next comes the stack; stack pointer initially points to top of it
	numPages += stackPages;
	initialSP = numPages*pageSize;

	// and finally reserve 1 page for arguments
	numPages++;

	if (!loadSections())
	    return false;

	// store arguments in last page
	int entryOffset = (numPages-1)*pageSize;
	int stringOffset = entryOffset + args.length*4;

	this.argc = args.length;
	this.argv = entryOffset;
	
	for (int i=0; i<argv.length; i++) {
	    byte[] stringOffsetBytes = Lib.bytesFromInt(stringOffset);
	    Lib.assertTrue(writeVirtualMemory(entryOffset,stringOffsetBytes) == 4);
	    entryOffset += 4;
	    Lib.assertTrue(writeVirtualMemory(stringOffset, argv[i]) ==
		       argv[i].length);
	    stringOffset += argv[i].length;
	    Lib.assertTrue(writeVirtualMemory(stringOffset,new byte[] { 0 }) == 1);
	    stringOffset += 1;
	}

	return true;
    }

    /**
     * Allocates memory for this process, and loads the COFF sections into
     * memory. If this returns successfully, the process will definitely be
     * run (this is the last step in process initialization that can fail).
     *
     * @return	<tt>true</tt> if the sections were successfully loaded.
     */
    protected boolean loadSections() {
	// physical page numbers allocated
		int[] physpagenums = UserKernel.allocatePage(numPages);

		if (physpagenums == null) {
			coff.close();
			Lib.debug(dbgProcess, "\tinsufficient physical memory");
			return false;
		}

		pageTable = new TranslationEntry[numPages];

		// load sections
		// the sections are contiguous and start at page 0
		for (int s = 0; s < coff.getNumSections(); s++) {
			CoffSection section = coff.getSection(s);

			Lib.debug(dbgProcess, "\tinitializing " + section.getName()
					+ " section (" + section.getLength() + " pages)");

			for (int i = 0; i < section.getLength(); i++) {
				int vpn = section.getFirstVPN() + i;
				int ppn = physpagenums[vpn];
				pageTable[vpn] = new TranslationEntry(vpn, ppn, true, section
						.isReadOnly(), false, false);
				section.loadPage(i, ppn);
			}
		}

		// allocate free pages for stack and argv
		for (int i = numPages - stackPages - 1; i < numPages; i++) {
			pageTable[i] = new TranslationEntry(i, physpagenums[i], true, false, false,
					false);
		}

		return true;
	}
    /**
     * Release any resources allocated by <tt>loadSections()</tt>.
     */
    protected void unloadSections() {
    		coff.close();
		for (int i = 0; i < numPages; i++)
			UserKernel.deallocatePage(pageTable[i].ppn);
		pageTable = null;
	} 

    /**
     * Initialize the processor's registers in preparation for running the
     * program loaded into this process. Set the PC register to point at the
     * start function, set the stack pointer register to point at the top of
     * the stack, set the A0 and A1 registers to argc and argv, respectively,
     * and initialize all other registers to 0.
     */
    public void initRegisters() {
	Processor processor = Machine.processor();

	// by default, everything's 0
	for (int i=0; i<processor.numUserRegisters; i++)
	    processor.writeRegister(i, 0);

	// initialize PC and SP according
	processor.writeRegister(Processor.regPC, initialPC);
	processor.writeRegister(Processor.regSP, initialSP);

	// initialize the first two argument registers to argc and argv
	processor.writeRegister(Processor.regA0, argc);
	processor.writeRegister(Processor.regA1, argv);
    }

    /**
     * Handle the halt() system call. 
     */
    private int handleHalt() {

	Machine.halt();
	
	Lib.assertNotReached("Machine.halt() did not halt machine!");
	return 0;
    }
	
	
	//----------------------------- CREATE/OPEN/WRITE/CLOSE/UNLINK -------------------------------//
	
	private int handleCreate(int name){
		//read filename from virtual memory 
		Lib.debug(dbgProcess, "handleCreate()");
		String fileName = readVirtualMemoryString(name,256); //max 256 bytes
		Lib.debug(dbgProcess, "filename: " + fileName);
		
		//open file by creating OpenFile object via StubFileSystem
		OpenFile file = UserKernel.fileSystem.open(fileName,true); //truncate = true it will create file with zero length if doesnt exist
		
		//check file
		if (file == null){
			return -1; // error
		}
		else{
			//check for open fdTable
			for(int i=2; i<MAXFD; i++){
				if(fdTable[i] == null){
					fdTable[i] = file;
					return i; // return value of fileDescriptor
				}
			}
		}
		
		return -1; //if no fdTable open...error
			
	}
	
	private int handleOpen(int name){
	
	
		//read filename from virtual memory 
		Lib.debug(dbgProcess, "handleOpen()");
		String fileName = readVirtualMemoryString(name,256); //max 256 bytes
		Lib.debug(dbgProcess, "filename: " + fileName);
		
		//check filename
		if (fileName == null){
			return -1; // error
		}
		
		//open file by creating OpenFile object via StubFileSystem
		OpenFile file = UserKernel.fileSystem.open(fileName,false);
		
		if(file == null){ // make sure file exists
			return -1;
		}
		else{
			//check for open fdTable
			for(int i=2; i<MAXFD; i++){
				if(fdTable[i] == null){
					fdTable[i] = file;
					return i; // return value of fileDescriptor
				}
			}
		}
		
		return -1; //if no fdTable open...error
		
	}
	
	private int handleWrite(int fileDescriptor, int buffer, int count){
		//check that fileDescriptor index is valid
		if(fileDescriptor < 0 || fileDescriptor > MAXFD){
			return -1; // error
		}
		
		//validate fileDescriptor points to a file
		OpenFile writeTo = fdTable[fileDescriptor];
		if(writeTo == null){
			return -1; // error
		}
		
		if(count == 0){
			return 0; //nothing to write so DONE 
		}		
		else{
			//access buffer
			byte[] bufferBytes = new byte[count]; //convert count to appropriate number of bytes
			int bytesRead = readVirtualMemory(buffer, bufferBytes);
				
			if( bytesRead < 0 ){
				return -1; //error
			}
				
			return writeTo.write(bufferBytes, 0, count); // write() returns number of bytes written
		}
	}
	
	private int handleRead(int fileDescriptor, int buffer, int count){
		//check that fileDescriptor index is valid
		if(fileDescriptor < 0 || fileDescriptor > MAXFD){
			return -1; // error
		}
		
		//validate fileDescriptor points to a file
		OpenFile readFrom = fdTable[fileDescriptor];
		if(readFrom == null){
			return -1; // error
		}
		else{
			if(count == 0)
				return 0; //nothing to read so done
			else{
				byte[] bufferBytes = new byte[count];
				int bytesRead = readFrom.read(bufferBytes, 0, count); //read from file accessed by fileDescriptor
				
				writeVirtualMemory(buffer, bufferBytes); //write bytes read to the buffer
				return bytesRead;
			}
		}
	}
	
	private int handleClose(int fileDescriptor){
		OpenFile file = fdTable[fileDescriptor];
		
		if (file == null){
			return -1; //error
		}
		else{
			fdTable[fileDescriptor].close();
			fdTable[fileDescriptor] = null; //make sure slot empty in table
			return 0; // success
			
		}
	}
	
	private int handleUnlink(int name){
		
		boolean removed = false;
		
		//get file name in String format
		String fileName = readVirtualMemoryString(name,256);
		
		//use handleOpen to find fileDescriptor if it exists
		int fileDescriptor = handleOpen(name);
		
		if(fileDescriptor == -1){
			//does not exist in fdTable
			removed = UserKernel.fileSystem.remove(fileName);
		}
		else{
			//close the file first
			fdTable[fileDescriptor].close();
			fdTable[fileDescriptor] = null;
			
			//delete file from the system
			removed = UserKernel.fileSystem.remove(fileName);
		}
		
		if(removed == true)
			return 0;
		else
			return -1;
	}
	
	//--------------------------------------------------------------------------------------------//


    private static final int
    syscallHalt = 0,
	syscallExit = 1,
	syscallExec = 2,
	syscallJoin = 3,
	syscallCreate = 4,
	syscallOpen = 5,
	syscallRead = 6,
	syscallWrite = 7,
	syscallClose = 8,
	syscallUnlink = 9;

    /**
     * Handle a syscall exception. Called by <tt>handleException()</tt>. The
     * <i>syscall</i> argument identifies which syscall the user executed:
     *
     * <table>
     * <tr><td>syscall#</td><td>syscall prototype</td></tr>
     * <tr><td>0</td><td><tt>void halt();</tt></td></tr>
     * <tr><td>1</td><td><tt>void exit(int status);</tt></td></tr>
     * <tr><td>2</td><td><tt>int  exec(char *name, int argc, char **argv);
     * 								</tt></td></tr>
     * <tr><td>3</td><td><tt>int  join(int pid, int *status);</tt></td></tr>
     * <tr><td>4</td><td><tt>int  creat(char *name);</tt></td></tr>
     * <tr><td>5</td><td><tt>int  open(char *name);</tt></td></tr>
     * <tr><td>6</td><td><tt>int  read(int fd, char *buffer, int size);
     *								</tt></td></tr>
     * <tr><td>7</td><td><tt>int  write(int fd, char *buffer, int size);
     *								</tt></td></tr>
     * <tr><td>8</td><td><tt>int  close(int fd);</tt></td></tr>
     * <tr><td>9</td><td><tt>int  unlink(char *name);</tt></td></tr>
     * </table>
     * 
     * @param	syscall	the syscall number.
     * @param	a0	the first syscall argument.
     * @param	a1	the second syscall argument.
     * @param	a2	the third syscall argument.
     * @param	a3	the fourth syscall argument.
     * @return	the value to be returned to the user.
     */
	 
	//changed handleSyscall to handle all Task I system calls
    public int handleSyscall(int syscall, int a0, int a1, int a2, int a3) {
	switch (syscall) {
		case syscallHalt:
		    return handleHalt();
		case syscallExit:
		case syscallExec:
		case syscallJoin:
		
		case syscallCreate:
			return handleCreate(a0);
		case syscallOpen:
			return handleOpen(a0);
		case syscallRead:
			return handleRead(a0, a1, a2);
		case syscallWrite:
			return handleWrite(a0, a1, a2);
		case syscallClose:
			return handleClose(a0);
		case syscallUnlink:
			return handleUnlink(a0);
	
	
		default:
		    Lib.debug(dbgProcess, "Unknown syscall " + syscall);
		    Lib.assertNotReached("Unknown system call!");
	}
	return 0;
    }

    /**
     * Handle a user exception. Called by
     * <tt>UserKernel.exceptionHandler()</tt>. The
     * <i>cause</i> argument identifies which exception occurred; see the
     * <tt>Processor.exceptionZZZ</tt> constants.
     *
     * @param	cause	the user exception that occurred.
     */
    public void handleException(int cause) {
	Processor processor = Machine.processor();

	switch (cause) {
	case Processor.exceptionSyscall:
	    int result = handleSyscall(processor.readRegister(Processor.regV0),
				       processor.readRegister(Processor.regA0),
				       processor.readRegister(Processor.regA1),
				       processor.readRegister(Processor.regA2),
				       processor.readRegister(Processor.regA3)
				       );
	    processor.writeRegister(Processor.regV0, result);
	    processor.advancePC();
	    break;				       
				       
	default:
	    Lib.debug(dbgProcess, "Unexpected exception: " +
		      Processor.exceptionNames[cause]);
	    Lib.assertNotReached("Unexpected exception");
	}
    }

    /** The program being run by this process. */
    protected Coff coff;

    /** This process's page table. */
    protected TranslationEntry[] pageTable;
   
    /** The number of contiguous pages occupied by the program. */
    protected int numPages;

    /** The number of pages in the program's stack. */
    protected final int stackPages = 8;
    
    private int initialPC, initialSP;
    private int argc, argv;
	
    private static final int pageSize = Processor.pageSize;
    private static final char dbgProcess = 'a';
    
    //ADDING FILE TABLE
    protected OpenFile[] fdTable;
    private static final int MAXFD = 16;
    
}
