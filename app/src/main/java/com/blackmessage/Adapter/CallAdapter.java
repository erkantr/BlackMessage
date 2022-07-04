package com.blackmessage.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackmessage.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import me.everything.providers.android.calllog.Call;
import me.everything.providers.android.calllog.CallsProvider;


public class CallAdapter extends RecyclerView.Adapter<CallAdapter.ViewHolder> implements Filterable {

    private Context mContext;
    private List<Call> calls;
    private List<Call> searchList;
    Call call;

    public CallAdapter(Context mContext, List<Call> calls) {
        this.calls = calls;
        this.mContext = mContext;
        CallsProvider callsProvider = new CallsProvider(mContext);
        this.searchList = callsProvider.getCalls().getList();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.call_item, parent, false);
        return new CallAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        call = calls.get(position);
        final String callnum = call.number;
        if (call.name == null) {
            holder.username.setText(call.number);
        } else {
            holder.username.setText(call.name);
        }


        Date callDayTime = new Date(Long.valueOf(call.callDate));
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yy HH:mm");
        final String dateString = formatter.format(callDayTime);
        holder.date.setText(dateString);

        holder.profile_image.setImageResource(R.drawable.user1);


        if (call.type == Call.CallType.OUTGOING) {
            holder.gelengiden.setImageResource(R.drawable.ic_baseline_call_made_24n);
            holder.gelengiden.setVisibility(View.VISIBLE);
        } else if (call.type == Call.CallType.INCOMING) {
            holder.gelengiden.setImageResource(R.drawable.ic_baseline_call_received_24);
            holder.gelengiden.setVisibility(View.VISIBLE);
        } else if (call.type == Call.CallType.MISSED) {
            holder.gelengiden.setImageResource(R.drawable.ic_baseline_call_missed_outgoing_24n);
        }

        holder.call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("tel:" + callnum);
                Intent intent = new Intent(Intent.ACTION_DIAL, uri);
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return calls.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            CallsProvider callsProvider = new CallsProvider(mContext);
            List<Call> filteredList = callsProvider.getCalls().getList();

            if (charSequence.toString().isEmpty()) {
                filteredList.addAll(searchList);
            } else {
                for (Call call : searchList) {
                    if (call.name.toLowerCase().contains(charSequence.toString().toLowerCase())) {
                        filteredList.add(call);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            calls.clear();
            calls.addAll((List<Call>) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        public ImageView gelengiden;
        public ImageView giden;
        public ImageView profile_image;
        public TextView date;
        public ImageView call;

        public ViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.image);
            gelengiden = itemView.findViewById(R.id.gelengiden);
//            giden = itemView.findViewById(R.id.giden);
            date = itemView.findViewById(R.id.date);
            call = itemView.findViewById(R.id.call);
        }
    }
}
