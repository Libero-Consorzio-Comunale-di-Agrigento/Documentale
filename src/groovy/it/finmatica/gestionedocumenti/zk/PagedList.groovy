package it.finmatica.gestionedocumenti.zk

/**
 * Created by DScandurra on 05/12/2017.
 */
class PagedList<T> extends ArrayList<T> {

    private final int totalCount

    PagedList(List<T> list, int totalCount) {
        super(list)
        this.totalCount = totalCount
    }

    int getTotalCount() {
        return totalCount
    }
}
