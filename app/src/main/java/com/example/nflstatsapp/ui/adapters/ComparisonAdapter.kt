package com.example.nflstatsapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nflstatsapp.R
import com.example.nflstatsapp.data.stats.Comparison

class ComparisonAdapter(private val comparisonList: List<Comparison>) : RecyclerView.Adapter<ComparisonAdapter.ComparisonViewHolder>() {

    // ViewHolder to hold the reference to the views
    class ComparisonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val statName: TextView = view.findViewById(R.id.statName)
        val statValue: TextView = view.findViewById(R.id.statValue)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComparisonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comparison, parent, false)
        return ComparisonViewHolder(view)
    }

    // Bind the comparison data to the views
    override fun onBindViewHolder(holder: ComparisonViewHolder, position: Int) {
        val comparison = comparisonList[position]

        // Determine which value is greater
        val player1Value = comparison.player1?.toDoubleOrNull() ?: 0.0
        val player2Value = comparison.player2?.toDoubleOrNull() ?: 0.0

        val formattedText: CharSequence = if (player1Value > player2Value) {
            // Highlight Player 1's value
            createSpannable(comparison.player1, comparison.player2, true)
        } else if (player2Value > player1Value) {
            // Highlight Player 2's value
            createSpannable(comparison.player1, comparison.player2, false)
        } else {
            // Both values are equal, no highlighting
            "${comparison.player1}      |       ${comparison.player2}"
        }

        holder.statName.text = comparison.statName
        holder.statValue.text = formattedText
    }

    private fun createSpannable(player1: String?, player2: String?, isPlayer1Higher: Boolean): CharSequence {
        val spannable = android.text.SpannableString("$player1      |       $player2")

        val color = android.graphics.Color.parseColor("#008042")
        val styleSpan = android.text.style.StyleSpan(android.graphics.Typeface.BOLD) // Bold style

        if (isPlayer1Higher) {
            // Highlight Player 1's value
            if (player1 != null) {
                spannable.setSpan(
                    android.text.style.ForegroundColorSpan(color),
                    0,
                    player1.length,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            if (player1 != null) {
                spannable.setSpan(
                    styleSpan,
                    0,
                    player1.length,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        } else {
            // Highlight Player 2's value
            val startIndex = player1?.length?.plus("      |       ".length)
            if (startIndex != null) {
                if (player2 != null) {
                    spannable.setSpan(
                        android.text.style.ForegroundColorSpan(color),
                        startIndex,
                        startIndex + player2.length,
                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
            if (startIndex != null) {
                if (player2 != null) {
                    spannable.setSpan(
                        styleSpan,
                        startIndex,
                        startIndex + player2.length,
                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }

        return spannable
    }


    // Return the size of the comparison list
    override fun getItemCount(): Int = comparisonList.size
}
