package com.mop.a2023.p20205151.JINJJAREAL.real

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.mop.a2023.p20205151.JINJJAREAL.R
import com.mop.a2023.p20205151.JINJJAREAL.databinding.ActivityMainBinding

import net.objecthunter.exp4j.ExpressionBuilder
import java.lang.Exception

// AppCompatActivity를 확장하는 MainActivity 클래스 선언
class MainActivity : AppCompatActivity() {

    // 배터리 레벨을 모니터링하기 위한 BroadcastReceiver
    private val batteryReceiver = BatteryReceiver()
    // 뷰 바인딩 인스턴스
    private lateinit var binding: ActivityMainBinding

    // 현재 수학적 표현식을 저장하는 문자열
    private var currentExpression = ""

    // 계산기 게임 변수 선언
    private var isGameMode = false
    private var currentProblemIndex = 0

    //난이도 조절 가능 현재 가장 쉬운 난이도. 난이도에 따라 암기력을 키울 수 있음
    //private val problems = listOf("121+123","243*253","335/34","434/42","23/45*56+23-2")
    private val problems = listOf("1+1","2*2","3/3","4/4")


    private var timer: CountDownTimer? = null
    private var remainingTime: Long = 0

    // 날짜와 내용을 저장하기 위한 데이터 변수
    private var data1: String? = null // 날짜 저장할 변수
    private var data2: String? = null // 내용 저장할 변수

    // onCreate 메서드: 액티비티 초기화가 수행되는 곳
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 뷰 바인딩 초기화
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        // BroadcastReceiver 등록
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, filter)

        // 버튼 ID 배열
        val buttonIds = arrayOf(
            binding.button0, binding.button1, binding.button2, binding.button3,
            binding.button4, binding.button5, binding.button6, binding.button7,
            binding.button8, binding.button9, binding.buttonDot,
            binding.buttonPlus, binding.buttonMinus, binding.buttonMultiply, binding.buttonDivide,
            binding.buttonOpenBracket, binding.buttonCloseBracket
        )

        // 각 버튼에 클릭 리스너 할당
        for (button in buttonIds) {
            button.setOnClickListener { onButtonClick(button.text.toString()) }
        }

        // 등호 버튼에 클릭 리스너 할당
        binding.buttonEquals.setOnClickListener { onEqualsButtonClick() }

        // 뒤로가기 버튼에 클릭 리스너 할당
        binding.buttonBack.setOnClickListener { onClearButtonClick() }

        // C 버튼에 클릭 리스너 할당
        binding.buttonC.setOnClickListener { onAllClearButtonClick() }


        // buttonStartGame 버튼에 클릭 리스너 할당
        binding.buttonStartGame.setOnClickListener { onStartGameButtonClick() }

        binding.addBtn?.setOnClickListener {
            // AddActivity로 이동하는 인텐트 설정
            val intent = Intent(this, AddActivity::class.java)
            // 액티비티 시작
            reqLauncher.launch(intent)
        }

    }
    // onDestroy 메서드: 액티비티 소멸 시 호출되는 메서드
    override fun onDestroy() {
        super.onDestroy()

        // BroadcastReceiver 해제
        unregisterReceiver(batteryReceiver)
    }

    // 게임 시작 함수
    private fun startGame() {
        isGameMode = true
        currentProblemIndex = 0
        showToast("지금부터 계산기 튜토리얼 게임을 시작합니다.")

        //게임 시작을 체크하기 위한 코드
        startTimer(60000)
        showNextProblem()
    }

    private fun startTimer(duration: Long) {
        timer?.cancel()
        timer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                // 필요한 경우 남은 시간으로 UI 업데이트
            }

            override fun onFinish() {
                endGame()
                showShareDialog()
            }
        }.start()
    }

    // 다음 문제 표시 함수
    private fun showNextProblem() {
        // 더 표시할 문제가 있는지 확인합니다.
        if (currentProblemIndex < problems.size) {
            // 리스트에서 다음 문제를 가져옵니다.
            val problem = problems[currentProblemIndex]

            // Toast를 사용하여 다음 문제에 대한 정보를 표시합니다.
            showToast("문제 ${currentProblemIndex + 1}: $problem")

            // 현재 표현식을 재설정하고 UI를 업데이트합니다.
            currentExpression = ""
            binding.solutionTv.text = currentExpression
            binding.resultTv.text = ""
        } else {
            // 더 이상 문제가 없으면 게임을 종료하고
            // 연속으로 틀린 횟수를 초기화합니다.
            endGame()
            consecutiveWrongCount = 0
        }
    }

    // 게임 종료 함수
    private fun endGame() {
        // 게임 모드를 종료하고 메시지를 Toast로 표시합니다.
        isGameMode = false
        showToast("튜토리얼 게임이 종료되었습니다.")

        // 현재 문제 인덱스를 초기화합니다.
        currentProblemIndex = 0

        // 타이머를 취소하여 시간이 멈추도록 합니다.
        timer?.cancel()

        // 연속으로 틀린 횟수를 초기화합니다.
        consecutiveWrongCount = 0

        // 배터리 잔량을 확인합니다.
        checkBatteryLevel()

        // 시간을 공유하는 다이얼로그를 표시합니다.
        showShareDialog()
    }

    // 배터리 잔량 확인 함수
    private fun checkBatteryLevel() {
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryLevel: Int = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        showToast("현재 배터리 잔량: $batteryLevel%")
    }

    // 게임 종료 후 시간을 공유하는 다이얼로그 표시 함수
    // 시간을 공유하는 다이얼로그를 표시하는 함수
    private fun showShareDialog() {
        // AlertDialog 인스턴스를 생성합니다.
        val alertDialog = AlertDialog.Builder(this)

        // 다이얼로그의 제목을 설정합니다.
        alertDialog.setTitle("시간을 공유하시겠습니까?")

        // "예" 버튼을 눌렀을 때의 동작을 정의합니다.
        alertDialog.setPositiveButton("예") { _, _ ->
            // 남은 시간을 초로 환산하여 토스트 메시지로 표시합니다.
            val remainingSeconds = remainingTime / 1000
            showToast("시간을 공유합니다: ${60 - remainingSeconds} 초")
        }

        // "아니오" 버튼을 눌렀을 때의 동작을 정의합니다.
        alertDialog.setNegativeButton("아니오") { _, _ ->
            // "시간을 공유하지 않습니다." 메시지를 토스트 메시지로 표시합니다.
            showToast("시간을 공유하지 않습니다.")
        }

        // AlertDialog를 생성하고 화면에 표시합니다.
        alertDialog.create().show()
    }


    // 토스트 메시지 표시 함수
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // 숫자 및 연산자 버튼 클릭 처리

    // 기존의 숫자 및 연산자 버튼 클릭 처리 함수
    private fun onButtonClick(value: String) {
        currentExpression += value
        binding.solutionTv.text = currentExpression
    }

    // 게임 중 연속으로 틀린 횟수를 저장하는 변수
    private var consecutiveWrongCount = 0


    // 등호 버튼 클릭 처리
    private fun onEqualsButtonClick() {
        // 게임 모드인 경우
        if (isGameMode) {
            try {
                // 입력된 표현식에서 '×'를 '*'로 대체하여 계산
                //혹시 모를 복사-붙여넣기 하였을 때 오류를 방지하기위해 작성
                val expression = currentExpression.replace("×", "*")
                val result = evaluateExpression(expression)

                // 현재 문제의 정답과 비교
                val correctAnswer = evaluateExpression(problems[currentProblemIndex])
                if (result == correctAnswer) {
                    // 정답일 경우 메시지를 표시하고 다음 문제로 진행
                    showToast("정답입니다!")
                    currentProblemIndex++
                    showNextProblem()

                    // 정답을 맞추면 연속으로 틀린 횟수 초기화
                    consecutiveWrongCount = 0
                } else {
                    // 틀렸을 경우 메시지를 표시하고 연속으로 틀린 횟수 증가
                    showToast("틀렸습니다. 다시 시도하세요.")
                    consecutiveWrongCount++

                    // 3번 연속으로 틀리면 게임 모드 종료
                    if (consecutiveWrongCount >= 3) {
                        showToast("0번 연속으로 틀려 게임 모드를 종료합니다.")

                        // 게임 종료 및 연속으로 틀린 횟수 초기화
                        endGame()
                        consecutiveWrongCount = 0
                    } else {
                        // 틀렸을 때 현재 문제 다시 보여주기
                        showNextProblem()
                    }
                }
            } catch (e: Exception) {
                // 오류가 발생한 경우 오류 메시지 표시
                showToast("오류")
            }
        } else {
            // 게임 모드가 아닌 경우 계산기 모드에서 동작
            try {
                // 입력된 표현식에서 '×'를 '*'로 대체하여 계산
                //혹시 모를 복사-붙여넣기 하였을 때 오류를 방지하기위해 작성
                val expression = currentExpression.replace("×", "*")
                val result = evaluateExpression(expression)

                // 계산 결과를 UI에 표시
                binding.resultTv.text = result.toString()

            } catch (e: Exception) {
                // 오류가 발생한 경우 오류 메시지 표시
                showToast("오류")
            }
        }
    }


    // 수식 평가 함수
    private fun evaluateExpression(expression: String): Double {
        val exp = ExpressionBuilder(expression).build()
        return exp.evaluate()
    }

    // 뒤로가기 버튼 클릭 처리
    private fun onClearButtonClick() {
        if (currentExpression.isNotEmpty()) {
            // 현재 표현식의 마지막 문자 제거
            currentExpression = currentExpression.substring(0, currentExpression.length - 1)
            binding.solutionTv.text = currentExpression
        }
    }

    // C 버튼 클릭 처리
    private fun onAllClearButtonClick() {
        // 모든 표현식 및 결과 초기화
        currentExpression = ""
        binding.solutionTv.text = ""
        binding.resultTv.text = ""
    }




    // ch13을 이용해서 기록을 추가하는 형식
    // 게임 시작 버튼 클릭 처리
    private fun onStartGameButtonClick() {
        startGame()
    }

    // AddActivity 결과를 처리하는 함수
    private fun processAddActivityResult(date: String?, result: String?) {
        // 입력된 날짜와 결과가 비어있지 않은 경우 처리
        if (!date.isNullOrBlank() && !result.isNullOrBlank()) {
            // gridLayout 변수를 통해 grid 레이아웃을 가져옵니다.
            val gridLayout = binding.gridLayout

            // 이미지뷰를 생성하고 할 일 이미지를 설정합니다.
            val imgView = ImageView(this)
            imgView.setImageResource(R.drawable.todo)
            imgView.setPadding(15)

            // gridLayout에 이미지뷰를 추가합니다.
            gridLayout?.addView(imgView)

            // 텍스트뷰를 생성하고 날짜와 결과를 표시합니다.
            val txtView = TextView(this).apply {
                text = "$date $result"
                textSize = 20f
                setPadding(15, 15, 15, 15)
            }

            // GridLayout의 행과 열을 설정합니다.
            val rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            val colSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            txtView.layoutParams = GridLayout.LayoutParams(rowSpec, colSpec)

            // gridLayout에 텍스트뷰를 추가합니다.
            gridLayout?.addView(txtView)
        }
    }

    // onActivityResult 함수 추가: AddActivity 결과를 처리하는 콜백
    private val reqLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // AddActivity의 결과가 OK인 경우 처리
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                // Intent에서 날짜와 결과를 가져와서 처리
                val date = data.getStringExtra("date")
                val result = data.getStringExtra("result")
                processAddActivityResult(date, result)
            }
        }
    }

    // 동반 객체에서 상수 ADD_ACTIVITY_REQUEST를 정의
    companion object {
        private const val ADD_ACTIVITY_REQUEST = 123
    }


}







