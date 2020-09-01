package tk.kvakva.cryptocurrencyprices

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import tk.kvakva.cryptocurrencyprices.databinding.CryptoItemBinding

class AdapterCrypto(val clickListener: RecyViewListener) : RecyclerView.Adapter<AdapterCrypto.ViewHolder>()  {
    var data = listOf<CryptoPrices>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size

    class ViewHolder private constructor(/*itemView: View*/ val binding: CryptoItemBinding) :
            RecyclerView.ViewHolder(/*itemView*/ binding.root) {

            fun bind(item: CryptoPrices
                     , clickListener: RecyViewListener
            ) {
                binding.crPair = item
                binding.recyviewLsnr = clickListener
                binding.executePendingBindings()
                //binding.imageView2.setImageBitmap(item.bitmap)
            }

            companion object {
                fun from(parent: ViewGroup, viewType: Int): ViewHolder {
                    val layoutInflater = LayoutInflater.from(parent.context)
                    val binding = CryptoItemBinding.inflate(layoutInflater, parent, false)
                    /*val view = layoutInflater
                        .inflate(R.layout.cont_item, parent, false)*/
                    return ViewHolder(binding)
                }
            }
        }

    /**
     * Called when RecyclerView needs a new [ViewHolder] of the given type to represent
     * an item.
     *
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     *
     *
     * The new ViewHolder will be used to display items of the adapter using
     * [.onBindViewHolder]. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary [View.findViewById] calls.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return A new ViewHolder that holds a View of the given view type.
     * @see .getItemViewType
     * @see .onBindViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, viewType)
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the [ViewHolder.itemView] to reflect the item at the given
     * position.
     *
     *
     * Note that unlike [android.widget.ListView], RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the `position` parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use [ViewHolder.getAdapterPosition] which will
     * have the updated adapter position.
     *
     * Override [.onBindViewHolder] instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the
     * item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        TODO("Not yet implemented")
//    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(data[position],clickListener)

}

class RecyViewListener(val clickListener: (view: View, cryptoPrices: CryptoPrices) -> Unit) {
    fun onClick(view: View, cryptoPrices: CryptoPrices) = clickListener(view, cryptoPrices)
}

