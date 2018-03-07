package cn.microanswer.desktop;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Microanswer on 2018/3/6.
 */

public class GridRecyclerViewAdapter extends RecyclerView.Adapter<GridRecyclerViewAdapter.AppItemViewHolder> {

    private ArrayList<AppItem> appItems;
    private MainActivity context;

    public GridRecyclerViewAdapter(MainActivity context) {
        this.context = context;
        appItems = new ArrayList<>();
    }

    public void setAppItems(List<AppItem> appItems) {
        this.appItems.clear();
        this.appItems.addAll(appItems);
        this.notifyDataSetChanged();
    }

    @Override
    public AppItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        AppItemView appItemView = new AppItemView(context);
        appItemView.setOnOpenApp(this.context);
        return new AppItemViewHolder(appItemView);
    }

    @Override
    public void onBindViewHolder(AppItemViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return appItems.size();
    }

    public class AppItemViewHolder extends RecyclerView.ViewHolder {

        public AppItemViewHolder(AppItemView itemView) {
            super(itemView);
        }

        public void bind(int position) {
            AppItemView appItemView = (AppItemView) itemView;
            appItemView.bind(appItems.get(position), position);
        }
    }
}
