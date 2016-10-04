package zgan.ohos.Models;

/**
 * Created by yajunsun on 16/10/4.
 */
public class ShoppingCartSummary {
    public String count;
    public String totalprice;
    public String oldtotalprice;

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
}
