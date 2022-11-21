package Memory;
import java.util.HashMap;

public class Cache {

	private HashMap<Integer, Integer> buffer; // address + data format
	private final int bufferSize = 4;
	private int [] writingBuffer;
	private boolean bufferFull= true;

	public Cache() {
         buffer = new HashMap<>();
         writingBuffer = new int[bufferSize];
         for(int i = 0; i < bufferSize; i++) {
        	 writingBuffer[i]=-1;
         }
	}
	

	/**
	 * Inserts a word into cache at the specified location
	 * 
	 * @param value - the value to be inserted
	 * @param location - the location in cache to insert the value into
	 */
	public void cacheInsert(int address, int data, Memory mem) {
		buffer.put(address, data);
		wbInsert(address, data, mem);
	}
	
	public void wbInsert(int address, int data, Memory mem) {
		for(int i = 0; i < bufferSize; i++) {
			if(writingBuffer[i] != -1) {
				writingBuffer[i] = address;
				bufferFull = false; // it's not full since inserted data into it
			}
		}
		if(bufferFull == true) { // writing Buffer is full
			mem.insert(writingBuffer[0], data); // insert into main memory
			writingBuffer[0] = writingBuffer[1]; // pop first and shift
			writingBuffer[1] = writingBuffer[2];
			writingBuffer[2] = writingBuffer[3];
			writingBuffer[3] = address;
		}
	}
	
	public boolean isBufferFull() {
		return bufferFull;
	}
	
	/**
	 * Gets a value in cache at the specified location
	 * 
	 * @param location - an string specifying the cache location
	 * @return - the value in cache at the specified location
	 */
	public int getData(int tag) {
		System.out.println("tag: "+tag);
		printCache();
		return buffer.get(tag);
	}
	
	public boolean inCache(int tag) {
		if(buffer.containsKey(tag)) {
			return true;
		}
		return false;
	}

	public HashMap<Integer, Integer> getCache() {
		return buffer;
	}

	public void printCache() {
		System.out.println(buffer);
	}
}
