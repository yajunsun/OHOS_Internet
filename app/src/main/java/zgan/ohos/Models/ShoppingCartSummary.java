package zgan.ohos.Models;

/**
 * Created by yajunsun on 16/10/4.
 */
public class ShoppingCartSummary {
    public String count="0";
    public String totalprice="0";
    public String oldtotalprice="0";


    public  String totalcount="0";

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getTotalprice() {
        return totalprice;
    }

    public void setTotalprice(String totalprice) {
        this.totalprice = totalprice;
    }

    public String getOldtotalprice() {
        return oldtotalprice;
    }

    public void setOldtotalprice(String oldtotalprice) {
        this.oldtotalprice = oldtotalprice;
    }
    public String getTotalcount() {
        return totalcount;
    }

    public void setTotalcount(String totalcount) {
        this.totalcount = totalcount;
    }
}
