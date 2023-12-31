package com.example.onlinedealapp.data.api;

import com.example.onlinedealapp.data.model.CartModel;
import com.example.onlinedealapp.data.model.CategoriesModel;
import com.example.onlinedealapp.data.model.InformationModel;
import com.example.onlinedealapp.data.model.ProductModel;
import com.example.onlinedealapp.data.model.RekeningModel;
import com.example.onlinedealapp.data.model.ResponseModel;
import com.example.onlinedealapp.data.model.TransactionsDetailModel;
import com.example.onlinedealapp.data.model.TransactionsModel;
import com.example.onlinedealapp.data.model.UserModel;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;

public interface UserService {
    @GET("user/getAllProduct")
    Call<List<ProductModel>> getAllProduct();

    @GET("user/getUserById")
    Call<UserModel> getMyProfile(
            @Query("id") String id
    );

    @GET("user/getCart")
    Call<List<CartModel>> getMyCart(
            @Query("id") String id
    );

    @FormUrlEncoded
    @POST("auth/register")
    Call<ResponseModel> register(
            @Field("email") String email,
            @Field("nama") String nama,
            @Field("password") String password,
            @Field("phone_number") String phoneNumber
    );


    @FormUrlEncoded
    @POST("user/addProductCart")
    Call<ResponseModel> addProductToCart(
            @Field("qty") Integer qty,
            @Field("user_id") String userId,
            @Field("product_id") String productId
    );

    @FormUrlEncoded
    @POST("user/deletecart")
    Call<ResponseModel> deleteCart(
            @Field("id_cart") String idCart,
            @Field("id_product") String idProduct,
            @Field("stock") Integer stock
    );

    @GET("user/getallrekening")
    Call<List<RekeningModel>> getallrekening();

    @FormUrlEncoded
    @POST("user/getInformationOrder")
    Call<InformationModel> getInformationOrder(
            @Field("user_id") String userId
    );

    @FormUrlEncoded
    @POST("user/updateAlamat")
    Call<ResponseModel> updateAlamat(
            @Field("user_id") String userId,
            @Field("address") String address,
            @Field("phone_number") String phoneNumber,
            @Field("postal_code") String postalCode
    );

    @FormUrlEncoded
    @POST("user/checkOut")
    Call<ResponseModel> checkOut(
            @Field("user_id") String userId,
            @Field("total_price") Integer totalPrice,
            @Field("city") String city,
            @Field("rek_id") Integer rekId,
            @Field("weight_total") Integer weightTotal
    );


    @GET("user/getMyTransactions")
    Call<List<TransactionsModel>> getMyTransactions (
            @Query("user_id") String userId
    );

    @Multipart
    @POST("user/uploadBuktiTransfer")
    Call<ResponseBody> uploadBuktiTransfer(
            @PartMap Map<String, RequestBody> textData,
            @Part MultipartBody.Part image
            );

    @GET("user/getProductByKategori")
    Call<List<ProductModel>> getProductByKategori(
            @Query("id") String id
    );


    @FormUrlEncoded
    @POST("user/updateProfile")
    Call<ResponseModel> updateProfile(
            @Field("user_id") String userId,
            @Field("password") String password,
            @Field("nama") String nama,
            @Field("telepon") String telepon,
            @Field("kode_pos") String kodePos

    );

    @GET("admin/getDetailTransactions")
    Call<List<TransactionsDetailModel>> getDetailTransactions(
            @Query("trans_id") String transId
    );

    @GET("user/getCategories")
    Call<List<CategoriesModel>> getCategories();

}
