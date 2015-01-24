package gr.forth.ics.urbanNet.utilities;
/**
 * 
 * Implementation of mergesort
 * source:http://www.roseindia.net/answers/viewqa/Java-Beginners/25294-Merge-Sort-String-Array-in-Java.html
 *Modified to return String[][] instead of String[] as well as the argument
 */
public class MergeSort {
	public static String[][] mergeSort(String[][] list) {
		String [][] sorted = new String[list.length][2];
	    if (list.length == 1) {
	        sorted = list;
	    } else {
	        int mid = list.length/2;
	        String[][] left = null; 
	        String[][] right = null;
	        if ((list.length % 2) == 0) {
	            left = new String[list.length/2][2];
	            right = new String[list.length/2][2];
	        } else {
	            left = new String[list.length/2][2];
	            right = new String[(list.length/2)+1][2];
	        }
	        int x=0;
	        int y=0;
	        for ( ; x < mid; x++) {
	            left[x] = list[x];
	        }
	        for ( ; x < list.length; x++) {
	            right[y++] = list[x];
	        }
	        left = mergeSort(left);
	        right = mergeSort(right);
	        sorted = mergeArray(left,right);
	    }

	    return sorted;
	}
/**
 * 
 * @param left the left half of the original array
 * @param right the right half of the original array
 * @return a 2D String array which consists of the 2 upper arrays
 */
	private static String[][] mergeArray(String[][] left, String[][] right) {
	    String[][] merged = new String[left.length+right.length][2];
	    int lIndex = 0;
	    int rIndex = 0;
	    int mIndex = 0;
	    double temp=0;
	    while (lIndex < left.length || rIndex < right.length) {
	        if (lIndex == left.length) {
	        	merged[mIndex][0] = right[rIndex][0];
	            merged[mIndex++][1] = right[rIndex++][1];
	        } else if (rIndex == right.length) {
	        	merged[mIndex][0] = left[lIndex][0];
	            merged[mIndex++][1] = left[lIndex++][1];
	        } else {  
	            temp = Double.parseDouble(left[lIndex][1])- Double.parseDouble(right[rIndex][1]);
	            if (temp > 0) {
	                merged[mIndex++] = right[rIndex++];
	            } else if (temp < 0) {
	                merged[mIndex++] = left[lIndex++];
	            } else { 
	                merged[mIndex++] = left[lIndex++];
	            }
	        }   
	    }
	    return merged;
	}
}