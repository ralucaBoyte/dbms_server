package dbms.utils;

import dbms.domain.Record;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MergeSort
{

    public Record[] mergeSort(Record[] a, boolean distinct, String sortingField) {
        if (a.length > 1) {
            int mid = a.length / 2;
            Record[] l = new Record[mid];
            Record[] r = new Record[a.length - mid];

            for (int i = 0; i < mid; i++) {
                l[i] = a[i];
            }
            for (int i = mid; i < a.length; i++) {
                r[i - mid] = a[i];
            }
            Record[] l1 = mergeSort(l, distinct, sortingField);
            Record[] l2 = mergeSort(r, distinct, sortingField);
            return merge(l1, l2, distinct, sortingField);
        }
        return a;
    }

    public Record[] merge(Record[] l, Record[] r, boolean distinct, String sortingField){
        int m = l.length;
        int n = r.length;
        int k = 0;
        int comparison = 0;
        Record []C = new Record[m+n];

        int i = 0, j = 0;
        while (i + j < m + n){
            if(i == m){
                C[k++] = r[j++];
            }
            else{
                if(j == n){
                    C[k++] = l[i++];
                }
                else
                {
                    comparison = compare(l[i], r[j], sortingField);
                    if(comparison == 0){
                        if(distinct){
                            C[k++] = r[j++];
                            i++;
                        }
                        else{
                            C[k++] = r[j++];
                        }

                    }
                    else{
                        if(comparison < 0){
                            C[k++] = l[i++];
                        }
                        else{
                            C[k++] = r[j++];
                        }
                    }
                }
            }
        }
        return C;
    }


    public Integer compare(Record record1, Record record2, String sortingField) {
        boolean ok = true;

        if(record1 == null){
            return 1;
        }
        if(record2 == null){
            return -1;
        }

        List<String> record1Value = convertRecordToListOfStrings(record1, sortingField);
        List<String> record2Value = convertRecordToListOfStrings(record2, sortingField);

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

    public List<String> convertRecordToListOfStrings(Record record, String sortingFields) {
        List values;
        if (sortingFields.equals("key")){
            values = Arrays.asList(record.getKey().split(";"));
        }
        else{
            values = Arrays.asList(record.getValue().split(";"));
        }

        return values;
    }

}
