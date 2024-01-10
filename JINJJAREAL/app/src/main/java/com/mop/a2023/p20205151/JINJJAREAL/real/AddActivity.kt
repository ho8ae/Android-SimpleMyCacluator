package com.mop.a2023.p20205151.JINJJAREAL.real

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mop.a2023.p20205151.JINJJAREAL.databinding.ActivityAddBinding
import java.util.Calendar



// AppCompatActivity를 확장하는 AddActivity 클래스 선언
class AddActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 뷰 바인딩 초기화
        val binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 현재 날짜로 Calendar 객체 초기화
        val calendar = Calendar.getInstance()

        // DatePickerDialog를 통해 날짜를 선택하고 텍스트뷰에 표시
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                binding.dataTxt.text = "$year 년 ${month + 1} 월 $dayOfMonth 일"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()

        // 저장 버튼 클릭 시 동작
        binding.saveBtn.setOnClickListener {
            // 결과를 담을 Intent 생성
            val resultIntent = Intent()

            // 선택한 날짜 문자열 생성
            val pickedDate = "(${calendar.get(Calendar.YEAR)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.DAY_OF_MONTH)})"

            // 결과 Intent에 날짜와 EditText 내용 추가
            resultIntent.putExtra("date", pickedDate)
            resultIntent.putExtra("result", binding.editTextView.text.toString())

            // 결과 코드와 함께 현재 액티비티 종료
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
