/**
 * CSCI 6461 - Fall 2022
 * 
 * Cache Class handles the indexing for a cahce level memory
 */

package Memory;
import java.util.HashMap;

public class Cache {

    // Buffer to save addresses, size for the buffer, etc.
	private HashMap<Integer, Integer> buffer; // address + data format
	private final int bufferSize = 4;
	private int [] writingBuffer;
	private boolean bufferFull = true;

	public Cache() {
         buffer = new HashMap<>();
         writingBuffer = new int[bufferSize];
         for(int i = 0; i < bufferSize; i++) {
        	 writingBuffer[i]=-1;
         }
	}
	
	// Inserts into cache at the specified location
	public void cacheInsert(int address, int data, Memory mem) {
		buffer.put(address, data);
		wbInsert(address, data, mem);
	}
	
    // Inserts into the writing buffer with data 
	public void wbInsert(int address, int data, Memory mem) {
		for(int i = 0; i < bufferSize; i++) {
			if(writingBuffer[i] != -1) {
				writingBuffer[i] = address;
				bufferFull = false; 
			}
		}
        // If the buffer is full, shift data to maintain the FIFO order
		if(bufferFull == true) { 
			mem.insert(writingBuffer[0], data); 
			writingBuffer[0] = writingBuffer[1];
			writingBuffer[1] = writingBuffer[2];
			writingBuffer[2] = writingBuffer[3];
			writingBuffer[3] = address;
		}
	}
	
	public boolean isBufferFull() { return bufferFull; }
	
	// Gets a value in cache at the specified location
	public int getData(int key) {
		System.out.println("tag: "+key);
		printCache();
		return buffer.get(key);
	}
	
    // Checks if a value is in the cache
	public boolean inCache(int key) {
		if(buffer.containsKey(key)) {
			return true;
		}
		return false;
	}

	public HashMap<Integer, Integer> getCache() { return buffer; }

	public void printCache() { System.out.println(buffer); }
}
