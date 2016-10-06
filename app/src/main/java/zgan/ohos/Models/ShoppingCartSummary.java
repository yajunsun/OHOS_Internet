package zgan.ohos.Models;

/**
 * Created by yajunsun on 16/10/4.
 */
public class ShoppingCartSummary {
    public String count;
    public String totalprice;
    public String oldtotalprice;


    public  String totalcount;

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
