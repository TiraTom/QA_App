package jp.techacademy.tiratom.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class QuestionDetailListAdapter(context: Context, private val mQuestion: Question) : BaseAdapter() {

    companion object {
        private val TYPE_QUESTION = 0
        private val TYPE_ANSWER = 1
    }

    private var mLayoutInflater: LayoutInflater? = null
    private lateinit var favoriteButton: ImageButton

    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getItemViewType(position: Int): Int {

        return if (position == 0) {
            TYPE_QUESTION
        } else {
            TYPE_ANSWER
        }
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var convertView = convertView

        if (getItemViewType(position) == TYPE_QUESTION) {
            if (convertView == null) {
                convertView = mLayoutInflater!!.inflate(R.layout.list_question_detail, parent, false)!!
            }
            val body = mQuestion.body
            val name = mQuestion.name

            val bodyTextView = convertView.findViewById<View>(R.id.bodyTextView) as TextView
            bodyTextView.text = body

            val nameTextView = convertView.findViewById<View>(R.id.nameTextView) as TextView
            nameTextView.text = name

            val bytes = mQuestion.imageBytes
            if (bytes.isNotEmpty()) {
                val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
                val imageView = convertView.findViewById<View>(R.id.imageView) as ImageView
                imageView.setImageBitmap(image)
            }

            favoriteButton = convertView.findViewById<View>(R.id.favoriteIcon) as ImageButton

            // お気に入りボタンにお気に入り登録・解除機能を設定
            favoriteButton.setOnClickListener { favoriteButton ->
                // TODO
            }

        } else {

            if (convertView == null) {
                convertView = mLayoutInflater!!.inflate(R.layout.list_answer, parent, false)!!
            }

            val answer = mQuestion.answers[position - 1]
            val body = answer.body
            val name = answer.name

            val bodyTextView = convertView.findViewById<View>(R.id.bodyTextView) as TextView
            bodyTextView.text = body

            val nameTextView = convertView.findViewById<View>(R.id.nameTextView) as TextView
            nameTextView.text = name

        }

        return convertView
    }

    override fun getItem(position: Int): Any {
        return mQuestion
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return 1 + mQuestion.answers.size
    }


}