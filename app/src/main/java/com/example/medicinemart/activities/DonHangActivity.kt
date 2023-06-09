package com.example.medicinemart.activities

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.example.medicinemart.R
import com.example.medicinemart.adapter.OnItemClickListener
import com.example.medicinemart.adapter.RecycleViewOrderAdapter
import com.example.medicinemart.common.Info
import com.example.medicinemart.common.Info._username
import com.example.medicinemart.common.Info.customer
import com.example.medicinemart.common.Info.time_defaul
import com.example.medicinemart.databinding.DonhangBinding
import com.example.medicinemart.models.Address
import com.example.medicinemart.models.Order
import com.example.medicinemart.models.Sanpham
import com.example.medicinemart.models.Time
import com.example.medicinemart.retrofit.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.sql.Date
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

public lateinit var binding_don_hang: DonhangBinding

var require_reload_data_order = false

var donHangChoXacNhanItemList = ArrayList<Order>()
var donHangDangGiaoItemList = ArrayList<Order>()
var donHangDaGiaoItemList = ArrayList<Order>()
var donHangDaHuyItemList = ArrayList<Order>()

private var progressDialog: ProgressDialog? = null


//var quantityChoXacNhan = ArrayList<Int>()
//var quantityDangGiao = ArrayList<Int>()
//var quantityDaGiao = ArrayList<Int>()
//var quantityDaHuy = ArrayList<Int>()


fun checkList(list: ArrayList<Order>) {
    if (list.isEmpty()) {
        binding_don_hang.layoutEmpty.visibility = View.VISIBLE
        binding_don_hang.recyclerView.visibility = View.INVISIBLE
    } else {
        binding_don_hang.layoutEmpty.visibility = View.INVISIBLE
        binding_don_hang.recyclerView.visibility = View.VISIBLE
    }
    println("Check list da thuc hien")
}


@RequiresApi(Build.VERSION_CODES.O)
fun loadDataDonhang() {
    val call = RetrofitClient.viewPagerApi.getOrder(customer.id, "All")
    call.enqueue(object : Callback<java.util.ArrayList<JsonObject>> {
        override fun onResponse(
            call: Call<java.util.ArrayList<JsonObject>>,
            response: Response<java.util.ArrayList<JsonObject>>
        ) {
            if (response.isSuccessful) {
                // Xử lý kết quả trả về nếu thêm hàng mới thành công
                println("Da load lai don hang thanh cong")
                println(response.body())
                require_reload_data_order = false
                if (progressDialog != null) {
                    progressDialog?.dismiss()
                    progressDialog = null
                }
                donHangChoXacNhanItemList.clear()
                donHangDangGiaoItemList.clear()
                donHangDaGiaoItemList.clear()
                donHangDaHuyItemList.clear()
                for (i in response.body()!!) {
                    val id_order = i.getAsJsonPrimitive("id_order").asInt
                    val quantity = i.getAsJsonPrimitive("quantity").asInt
                    var status = i.getAsJsonPrimitive("status").toString()
                    status = status.substring(1, status.length - 1)
                    val id = i.getAsJsonPrimitive("id").asInt
                    val name = i.getAsJsonPrimitive("name").toString()
                    val type = i.getAsJsonPrimitive("type").toString()
                    val price = i.getAsJsonPrimitive("price").asInt
                    val describe = i.getAsJsonPrimitive("describe").toString()
                    val image = i.getAsJsonPrimitive("image").toString()
                    val ingredient = i.getAsJsonPrimitive("ingredient").toString()
                    val user_guide = i.getAsJsonPrimitive("user_guide").toString()
                    var barcode = ""
                    if (!i.get("barcode").isJsonNull) {
                        barcode = i.getAsJsonPrimitive("barcode").toString()
                    } else {
                        barcode = "1"
                    }

                    val id_address = i.getAsJsonPrimitive("id_address").asInt
                    val full_name = i.getAsJsonPrimitive("full_name").asString
                    val phone = i.getAsJsonPrimitive("phone").asString
                    val td_x = i.getAsJsonPrimitive("td_x").asBigDecimal
                    val td_y = i.getAsJsonPrimitive("td_y").asBigDecimal
                    val location = i.getAsJsonPrimitive("location").asString

                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    var _date = i.getAsJsonPrimitive("orderDate").asString
                    val _orderdate = LocalDateTime.parse(_date, formatter)

                    var _shipdate = time_defaul
                    if (!i.get("shipDate").isJsonNull) {
                        _date = i.getAsJsonPrimitive("shipDate")?.asString
                        _shipdate = LocalDateTime.parse(_date.toString(), formatter)
                    } else {
                        _shipdate = time_defaul
                    }

                    var _receiveddate = time_defaul
                    if (!i.get("receivedDate").isJsonNull) {
                        _date = i.getAsJsonPrimitive("receivedDate")?.asString
                        _receiveddate = LocalDateTime.parse(_date.toString(), formatter)
                    } else {
                        _receiveddate = time_defaul
                    }

                    var _canceldate = time_defaul
                    if (!i.get("cancelDate").isJsonNull) {
                        _date = i.getAsJsonPrimitive("cancelDate")?.asString
                        _canceldate = LocalDateTime.parse(_date.toString(), formatter)
                    } else {
                        _canceldate = time_defaul
                    }

                    val sanpham = Sanpham(id, name, type, price, describe, ingredient, user_guide, image, barcode)
                    val address = Address(id_address, phone, _username, full_name, td_x, td_y, location)
                    val time = Time(_orderdate, _shipdate, _receiveddate, _canceldate)
                    val tmp = Order(id_order, status, sanpham, quantity, address, time)

                    if (status == "Chờ xác nhận") {
                        donHangChoXacNhanItemList.add(tmp)
//                quantityChoXacNhan.add(i.getAsJsonPrimitive("quantity").asInt)
                    } else if (status == "Đang giao") {
                        donHangDangGiaoItemList.add(tmp)
//                quantityDangGiao.add(i.getAsJsonPrimitive("quantity").asInt)
                    } else if (status == "Đã giao") {
                        donHangDaGiaoItemList.add(tmp)
//                quantityDaGiao.add(i.getAsJsonPrimitive("quantity").asInt)
                    } else if (status == "Đã hủy") {
                        donHangDaHuyItemList.add(tmp)
//                quantityDaHuy.add(i.getAsJsonPrimitive("quantity").asInt)
                    }
                }
                binding_don_hang.recyclerView.adapter?.notifyDataSetChanged()
                checkList(donHangChoXacNhanItemList)

            } else {
                println(response.errorBody())
                println("Load don hang fail donhangactivity")
                // Xử lý lỗi nếu thêm hàng mới thất bại
            }
        }

        override fun onFailure(call: Call<java.util.ArrayList<JsonObject>>, t: Throwable) {
            // Xử lý lỗi nếu không thể kết nối tới server

        }
    })
}

//fun loadDataDonhang() {
//    GlobalScope.launch(Dispatchers.Main) {
//        donHangChoXacNhanItemList.clear()
//        donHangDangGiaoItemList.clear()
//        donHangDaGiaoItemList.clear()
//        donHangDaHuyItemList.clear()
//        println("custom_id " + customer.id)
//        val getOrder = async { RetrofitClient.viewPagerApi.getOrder(customer.id, "All") }
//        val res_getOrder = getOrder.await().body()
//        for (i in res_getOrder!!) {
//            val id_order = i.getAsJsonPrimitive("id_order").asInt
//            val quantity = i.getAsJsonPrimitive("quantity").asInt
//            val id = i.getAsJsonPrimitive("id").asInt
//            val name = i.getAsJsonPrimitive("name").toString()
//            val type = i.getAsJsonPrimitive("type").toString()
//            val price = i.getAsJsonPrimitive("price").asInt
//            val describe = i.getAsJsonPrimitive("describe").toString()
//            val image = i.getAsJsonPrimitive("image").toString()
//            var status = i.getAsJsonPrimitive("status").toString()
//            status = status.substring(1, status.length - 1)
//            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
//
//            var _date = i.getAsJsonPrimitive("orderDate").asString
//            val _orderdate = LocalDateTime.parse(_date, formatter)
//
//            var _shipdate = time_defaul
//            if (!i.get("shipDate").isJsonNull) {
//                _date = i.getAsJsonPrimitive("shipDate")?.asString
//                _shipdate = LocalDateTime.parse(_date.toString(), formatter)
//            } else {
//                _shipdate = time_defaul
//            }
//
//            var _receiveddate = time_defaul
//            if (!i.get("shipDate").isJsonNull) {
//                _date = i.getAsJsonPrimitive("receivedDate")?.asString
//                _receiveddate = LocalDateTime.parse(_date.toString(), formatter)
//            } else {
//                _receiveddate = time_defaul
//            }
//
//            var _canceldate = time_defaul
//            if (!i.get("shipDate").isJsonNull) {
//                _date = i.getAsJsonPrimitive("cancelDate")?.asString
//                _canceldate = LocalDateTime.parse(_date.toString(), formatter)
//            } else {
//                _canceldate = time_defaul
//            }
//
//            val sanpham = Sanpham(id, name, type, price, describe, image)
//            val address = Address()
//            val time = Time(_orderdate, _shipdate, _receiveddate, _canceldate)
//            val tmp = Order(id_order, sanpham, quantity, address, time)
//            if (status == "Chờ xác nhận") {
//                donHangChoXacNhanItemList.add(tmp)
////                quantityChoXacNhan.add(i.getAsJsonPrimitive("quantity").asInt)
//            } else if (status == "Đang giao") {
//                donHangDangGiaoItemList.add(tmp)
////                quantityDangGiao.add(i.getAsJsonPrimitive("quantity").asInt)
//            } else if (status == "Đã giao") {
//                donHangDaGiaoItemList.add(tmp)
////                quantityDaGiao.add(i.getAsJsonPrimitive("quantity").asInt)
//            } else if (status == "Đã hủy") {
//                donHangDaHuyItemList.add(tmp)
////                quantityDaHuy.add(i.getAsJsonPrimitive("quantity").asInt)
//            }
//        }
//    }
//}

//@RequiresApi(Build.VERSION_CODES.O)
//fun convertDateToLocalDateTime(date: Date): LocalDateTime {
//    val instant = Instant.ofEpochMilli(date.time)
//    return LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
//}

//fun loadDataDonhang() {
//    GlobalScope.launch(Dispatchers.Main) {
//        donHangChoXacNhanItemList.clear()
//        donHangDangGiaoItemList.clear()
//        donHangDaGiaoItemList.clear()
//        donHangDaHuyItemList.clear()
//        println("custom_id " + customer.id)
//        val getOrder = async { RetrofitClient.viewPagerApi.getOrder(customer.id, "All") }
//        val res_getOrder = getOrder.await().body()
//        for (i in res_getOrder!!) {
//            val id_order = i.getAsJsonPrimitive("id_order").asInt
//            val quantity = i.getAsJsonPrimitive("quantity").asInt
//            val id = i.getAsJsonPrimitive("id").asInt
//            val name = i.getAsJsonPrimitive("name").toString()
//            val type = i.getAsJsonPrimitive("type").toString()
//            val price = i.getAsJsonPrimitive("price").asInt
//            val describe = i.getAsJsonPrimitive("describe").toString()
//            val image = i.getAsJsonPrimitive("image").toString()
//            var status = i.getAsJsonPrimitive("status").toString()
//            status = status.substring(1, status.length - 1)
//            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
//
//            var _date = i.getAsJsonPrimitive("orderDate").asString
//            val _orderdate = LocalDateTime.parse(_date, formatter)
//
//            var _shipdate = time_defaul
//            if (!i.get("shipDate").isJsonNull) {
//                _date = i.getAsJsonPrimitive("shipDate")?.asString
//                _shipdate = LocalDateTime.parse(_date.toString(), formatter)
//            } else {
//                _shipdate = time_defaul
//            }
//
//            var _receiveddate = time_defaul
//            if (!i.get("shipDate").isJsonNull) {
//                _date = i.getAsJsonPrimitive("receivedDate")?.asString
//                _receiveddate = LocalDateTime.parse(_date.toString(), formatter)
//            } else {
//                _receiveddate = time_defaul
//            }
//
//            var _canceldate = time_defaul
//            if (!i.get("shipDate").isJsonNull) {
//                _date = i.getAsJsonPrimitive("cancelDate")?.asString
//                _canceldate = LocalDateTime.parse(_date.toString(), formatter)
//            } else {
//                _canceldate = time_defaul
//            }
//
//            val sanpham = Sanpham(id, name, type, price, describe, image)
//            val address = Address()
//            val time = Time(_orderdate, _shipdate, _receiveddate, _canceldate)
//            val tmp = Order(id_order, sanpham, quantity, address, time)
//            if (status == "Chờ xác nhận") {
//                donHangChoXacNhanItemList.add(tmp)
////                quantityChoXacNhan.add(i.getAsJsonPrimitive("quantity").asInt)
//            } else if (status == "Đang giao") {
//                donHangDangGiaoItemList.add(tmp)
////                quantityDangGiao.add(i.getAsJsonPrimitive("quantity").asInt)
//            } else if (status == "Đã giao") {
//                donHangDaGiaoItemList.add(tmp)
////                quantityDaGiao.add(i.getAsJsonPrimitive("quantity").asInt)
//            } else if (status == "Đã hủy") {
//                donHangDaHuyItemList.add(tmp)
////                quantityDaHuy.add(i.getAsJsonPrimitive("quantity").asInt)
//            }
//        }
//    }
//}

@RequiresApi(Build.VERSION_CODES.O)
fun convertDateToLocalDateTime(date: Date): LocalDateTime {
    val instant = Instant.ofEpochMilli(date.time)
    return LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
}

class DonHangActivity : AppCompatActivity(), View.OnClickListener {
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ObjectAnimatorBinding")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding_don_hang = DonhangBinding.inflate(layoutInflater)
        setContentView(binding_don_hang.root)

        var badge = binding_don_hang.bottomNavigationView.getOrCreateBadge(R.id.thongbao)
        if (Info.so_thong_bao_chua_doc == 0) {
            badge.isVisible = false
        } else {
            badge.isVisible = true
            badge.number = Info.so_thong_bao_chua_doc
        }
//        handlerButton(binding_don_hang.btnChoXacNhan)
        handlerData(binding_don_hang.btnChoXacNhan)

        binding_don_hang.bottomNavigationView.setSelectedItemId(R.id.donhang)
        val mOnNavigationItemSelectedListener =
            BottomNavigationView.OnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.thongbao -> {
                        // put your code here
                        val intent = Intent(this@DonHangActivity, ThongBaoActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.no_animation,  R.anim.no_animation)
                        return@OnNavigationItemSelectedListener true
                    }

                    R.id.hoso -> {
                        // put your code here
                        val intent = Intent(this@DonHangActivity, HoSoActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.no_animation,  R.anim.no_animation)
                        return@OnNavigationItemSelectedListener true
                    }
                    R.id.home -> {
                        // put your code here
                        val intent = Intent(this@DonHangActivity, TrangChuActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.no_animation,  R.anim.no_animation)
                        return@OnNavigationItemSelectedListener true
                    }
                }
                false
            }

        binding_don_hang.bottomNavigationView.setOnNavigationItemSelectedListener(
            mOnNavigationItemSelectedListener
        )


        binding_don_hang.btnChoXacNhan.setOnClickListener {
            handlerButton(binding_don_hang.btnChoXacNhan)
            handlerData(binding_don_hang.btnChoXacNhan)
        }
        binding_don_hang.btnDangGiao.setOnClickListener {
            handlerButton(binding_don_hang.btnDangGiao)
            handlerData(binding_don_hang.btnDangGiao)
        }
        binding_don_hang.btnDaGiao.setOnClickListener {
            handlerButton(binding_don_hang.btnDaGiao)
            handlerData(binding_don_hang.btnDaGiao)
        }
        binding_don_hang.btnDaHuy.setOnClickListener {
            handlerButton(binding_don_hang.btnDaHuy)
            handlerData(binding_don_hang.btnDaHuy)
        }

        binding_don_hang.btnChoXacNhan.performClick()


    }

    override fun onClick(p0: View?) {
        TODO("Not yet implemented")
    }

//    fun checkList(list: ArrayList<Order>) {
//        if (list.isEmpty()) {
//            binding_don_hang.layoutEmpty.visibility = View.VISIBLE
//            binding_don_hang.recyclerView.visibility = View.INVISIBLE
//
//        } else {
//            binding_don_hang.layoutEmpty.visibility = View.INVISIBLE
//            binding_don_hang.recyclerView.visibility = View.VISIBLE
//        }
//    }

    fun handlerButton(p: AppCompatButton) {
        binding_don_hang.btnDangGiao.setActivated(false)
        binding_don_hang.btnDaGiao.setActivated(false)
        binding_don_hang.btnChoXacNhan.setActivated(false)
        binding_don_hang.btnDaHuy.setActivated(false)
        binding_don_hang.btnDangGiao.setTextColor(Color.parseColor("#D3D3D3"))
        binding_don_hang.btnDaGiao.setTextColor(Color.parseColor("#D3D3D3"))
        binding_don_hang.btnChoXacNhan.setTextColor(Color.parseColor("#D3D3D3"))
        binding_don_hang.btnDaHuy.setTextColor(getResources().getColor(R.color.grey))
        p.setActivated(true)
        p.setTextColor(Color.parseColor("#FFFFFF"))

    }

    fun handlerData(p: AppCompatButton) {
//        var list = ArrayList<Sanpham>()
        var list = donHangChoXacNhanItemList
//        var quantity_product_in_order = quantityChoXacNhan
        when (p) {
            binding_don_hang.btnChoXacNhan -> {
                list = donHangChoXacNhanItemList
//                quantity_product_in_order = quantityChoXacNhan
            }
            binding_don_hang.btnDangGiao -> {
                list = donHangDangGiaoItemList
//                quantity_product_in_order = quantityDangGiao
            }
            binding_don_hang.btnDaGiao -> {
                list = donHangDaGiaoItemList
//                quantity_product_in_order = quantityDaGiao
            }
            binding_don_hang.btnDaHuy -> {
                list = donHangDaHuyItemList
//                quantity_product_in_order = quantityDaHuy
            }
        }
        checkList(list)
        if (!list.isEmpty()) {
            val adapterRecyclerOrder =
                RecycleViewOrderAdapter(list, this, object :
                    OnItemClickListener {
                    override fun onItemClick(position: Int) {
                        val intent = Intent(this@DonHangActivity, ThongTinDonHangActivity::class.java)
                        order_detail = Order()
                        order_detail.status = list.get(position).status
                        order_detail.sanpham = list.get(position).sanpham
                        order_detail.address = list.get(position).address
                        order_detail.time = list.get(position).time
                        order_detail.id = list.get(position).id
                        order_detail.quantity = list.get(position).quantity
                        intent.putExtra("type_order", order_detail.status)
                        startActivity(intent)
                        Animatoo.animateSlideLeft(this@DonHangActivity)
                    }
                })
            binding_don_hang.recyclerView.layoutManager = LinearLayoutManager(this)

            val VerticalLayout = LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
            )
            binding_don_hang.recyclerView.setLayoutManager(VerticalLayout)
            binding_don_hang.recyclerView.adapter = adapterRecyclerOrder
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()

        Info.doubleBackPressed = false
        binding_don_hang.bottomNavigationView.setSelectedItemId(R.id.donhang)

        loadDataDonhang()
        if (require_reload_data_order == true) {
            progressDialog = ProgressDialog(this)
            progressDialog?.setCancelable(false)
            progressDialog?.setMessage("Đợi xíu...")
            progressDialog?.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            progressDialog?.setProgress(0)
            progressDialog?.show()
            loadDataDonhang()
        }

        order_detail = Order()
        binding_don_hang.recyclerView.adapter?.notifyDataSetChanged()

        var badge = binding_don_hang.bottomNavigationView.getOrCreateBadge(R.id.thongbao)
        if (Info.so_thong_bao_chua_doc == 0) {
            badge.isVisible = false
        } else {
            badge.isVisible = true
            badge.number = Info.so_thong_bao_chua_doc
        }
    }

    override fun onBackPressed() {
        if (Info.doubleBackPressed) {
            super.onBackPressed()
            finishAffinity()
            return
        }

        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val tasks = activityManager.getRunningTasks(1) // Lấy danh sách task chạy hiện tại

//        if (tasks.isNotEmpty() && tasks[0].numActivities == 1) {
        // Chỉ có một activity duy nhất trong stack
        Info.doubleBackPressed = true
        Toast.makeText(this, "Chạm lần nữa để thoát", Toast.LENGTH_SHORT).show()
//        } else {
//            super.onBackPressed()
//        }

        doubleBackToExitHandler.postDelayed({
            Info.doubleBackPressed = false
        }, Info.doubleBackDelay)
    }


}
