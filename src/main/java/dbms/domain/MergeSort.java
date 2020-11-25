package dbms.domain;

import java.util.Arrays;
import java.util.List;

public class MergeSort
{
    // Merges two subarrays of arr[].
    // First subarray is arr[l..m]
    // Second subarray is arr[m+1..r]
    public void merge(Record arr[], int l, int m, int r)
    {
        // Find sizes of two subarrays to be merged
        int n1 = m - l + 1;
        int n2 = r - m;

        // Create temp arrays /
        Record L[] = new Record[n1];
        Record R[] = new Record[n2];

        /*Copy data to temp arrays*/
        for (int i = 0; i < n1; ++i)
            L[i] = arr[l + i];
        for (int j = 0; j < n2; ++j)
            R[j] = arr[m + 1 + j];

        // Merge the temp arrays /

        // Initial indexes of first and second subarrays
        int i = 0, j = 0;

        // Initial index of merged subarry array
        int k = l;
        Integer comparison = 0;
        while (i < n1 && j < n2) {
            comparison = compare(L[i], R[j]);
            if(comparison == 0){
                arr[k] = L[i];
                i++;
                j++;
            }
            else{
                if (comparison < 0) {
                    arr[k] = L[i];
                    i++;
                }
                else {
                    arr[k] = R[j];
                    j++;
                }
            }
            k++;
        }

        // Copy remaining elements of L[] if any /
        while (i < n1) {
            arr[k] = L[i];
            i++;
            k++;
        }

        //Copy remaining elements of R[] if any /
        while (j < n2) {
            arr[k] = R[j];
            j++;
            k++;
        }
    }

    // Main function that sorts arr[l..r] using
    // merge()
    public void sort(Record arr[], int l, int r)
    {
        if (l < r) {
            // Find the middle point
            int m = (l + r) / 2;

            // Sort first and second halves
            sort(arr, l, m);
            sort(arr, m + 1, r);

            // Merge the sorted halves
            merge(arr, l, m, r);
        }
    }



    public Integer compare(Record record1, Record record2) {
        boolean ok = true;
        List<String> record1Value = convertRecordToListOfStrings(record1);
        List<String> record2Value = convertRecordToListOfStrings(record2);

        Integer comparison = 0;
        for(int i = 0; i < record1Value.size() && ok; i++){
            comparison = record1Value.get(i).compareTo(record2Value.get(i));
            if(comparison > 0 || comparison < 0){
                ok = false;
                break;
            }
        }
        return comparison;
    }

    public List<String> convertRecordToListOfStrings(Record record) {
        List<String> values = Arrays.asList(record.getValue().split(";"));
        return values;
    }

}
