package zgan.ohos.ConstomControls.SortView;

public class SortModel {

    private String name;
    private String sortLetters;
    //未排序之前源数据集合的索引，用于方便定位到原数据
    private int sourceIndex;


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getSortLetters() {
        return sortLetters;
    }
    public void setSortLetters(String sortLetters) {
        this.sortLetters = sortLetters;
    }

    public int getSourceIndex(){return sourceIndex;}
    public void setSourceIndex(int index){this.sourceIndex=index;}
}
