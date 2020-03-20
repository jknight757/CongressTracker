package com.example.congresstracker.models;

import java.util.HashMap;

public class States {

    private static String[] stateAbList = new String[] {"AK","AL","AR","AZ","CA","CO","CT","DE","FL","GA","HI","IA","ID",
            "IL","IN","KS","KY","LA","MA","MD","ME","MI","MN","MO","MS","MT","NC","ND","NE","NH","NJ","NM","NV","NY",
            "OH","OK","OR","PA","RI","SC","SD","TN","TX","UT","VA","VT","WA","WI","WV","WY"};

    private static String[] stateList = new String[] {"Alaska","Alabama","Arkansas","Arizona","California","Colorado","Connecticut",
            "Delaware","Florida","Georgia","Hawaii","Iowa","Idaho", "Illinois","Indiana","Kansas",
            "Kentucky","Louisiana","Massachusetts","Maryland","Maine","Michigan", "Minnesota","Missouri","Mississippi",
            "Montana","North Carolina","North Dakota","Nebraska","New Hampshire", "New Jersey","New Mexico","Nevada",
            "New York", "Ohio","Oklahoma","Oregon","Pennsylvania","Rhode Island","South Carolina","South Dakota",
            "Tennessee","Texas","Utah", "Virginia","Vermont","Washington","Wisconsin","West Virginia","Wyoming"};

    private static HashMap<String, Integer> stateDistrictNums = new HashMap<String, Integer>(){{
       put("AK",1);
       put("AL",7);
       put("AR",4);
       put("AZ",8);
       put("CA",53);
       put("CO",7);
       put("CT",5);
       put("DE",1);
       put("FL",25);
       put("GA",13);
       put("HI",2);
       put("IA",5);
       put("ID",2);
       put("IL",19);
       put("IN",9);
       put("KS",4);
       put("KY",6);
       put("LA",7);
       put("MA",10);
       put("MD",8);
       put("ME",2);
       put("MI",15);
       put("MN",8);
       put("MO",9);
       put("MS",4);
       put("MT",1);
       put("NC",13);
       put("ND",1);
       put("NE",3);
       put("NH",2);
       put("NJ",13);
       put("NM",3);
       put("NV",3);
       put("NY",29);
       put("OH",18);
       put("OK",5);
       put("OR",5);
       put("PA",19);
       put("RI",2);
       put("SC",6);
       put("SD",1);
       put("TN",9);
       put("TX",32);
       put("UT",3);
       put("VA",11);
       put("VT",1);
       put("WA",9);
       put("WI",8);
       put("WV",3);
       put("WY",1);
    }};

   public static int getDistricts(String abr){
      if(stateDistrictNums.containsKey(abr)){
         return stateDistrictNums.get(abr);
      }
      return 0;
   }
   public static String getUnabreviated(String abr){
      String state = "";
      for (int i = 0; i < stateAbList.length; i++) {
         if(stateAbList[i].equals(abr)){
            state = stateList[i];
         }

      }
      return state;
   }
   public static String getAbreviation(String st){
      String abrv = "";
      for (int i = 0; i < stateList.length; i++) {
         if(stateList[i].equals(st)){
            abrv = stateAbList[i];
         }

      }
      return abrv;
   }

}
