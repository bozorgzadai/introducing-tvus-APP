package com.bozorgzad.ali.introducingtvus;

/*
 * Created by Ali_Dev on 9/4/2017.
 */

class ConvertDate {
    String gregorianToJalali(int day, int month, int year){
        return calcGregorianToJalali(day, month, year);
    }

    private String calcGregorianToJalali(int g_d, int g_m, int g_y)
    {

        int[] g_days_in_month = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        int[] j_days_in_month = {31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29};

        int gy = g_y-1600;
        int gm = g_m-1;
        int gd = g_d-1;

        int g_day_no = 365*gy+div(gy+3,4)-div(gy+99,100)+div(gy+399,400);

        for (int i=0; i < gm; ++i)
            g_day_no += g_days_in_month[i];
        if (gm>1 && ((gy%4==0 && gy%100!=0) || (gy%400==0)))
            // leap and after Feb
            g_day_no++;
        g_day_no += gd;

        int j_day_no = g_day_no-79;

        int j_np = div(j_day_no, 12053); //12053 = 365*33 + 32/4
        j_day_no = j_day_no % 12053;

        int jy = 979+33*j_np+4*div(j_day_no,1461); // 1461 = 365*4 + 4/4

        j_day_no %= 1461;

        if (j_day_no >= 366) {
            jy += div(j_day_no-1, 365);
            j_day_no = (j_day_no-1)%365;
        }

        int j;
        for (j=0; j < 11 && j_day_no >= j_days_in_month[j]; ++j)
            j_day_no -= j_days_in_month[j];
        int jm = j+1;
        int jd = j_day_no+1;


        return (jy+"/"+jm+"/"+jd);
    }

    private int div(float a, float b)
    {
        return (int)(a/b);
    }

}
