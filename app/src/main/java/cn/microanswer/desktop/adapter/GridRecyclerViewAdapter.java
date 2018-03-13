package cn.microanswer.desktop.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.microanswer.desktop.data.AppItem;
import cn.microanswer.desktop.ui.AppItemView;
import cn.microanswer.desktop.ui.MainActivity;

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

    public void addAppItem(AppItem appItem) {
        if (appItems == null) {
            appItems = new ArrayList<>();
            appItems.add(appItem);
            notifyDataSetChanged();
        } else {
            if (appItems.contains(appItem)) {
                return;
            } else {
                appItems.add(appItem);
                Collections.sort(appItems, new Comparator<AppItem>() {
                    @Override
                    public int compare(AppItem o1, AppItem o2) {
                        return o2.getName().compareTo(o1.getName());
                    }
                });
                notifyDataSetChanged();
            }
        }
    }

    public void removeAppItem(String packag2e) {
        if (appItems == null) {
            return;
        }
        int i = appItems.indexOf(new AppItem(packag2e));
        appItems.remove(i);
        notifyItemRemoved(i);
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
