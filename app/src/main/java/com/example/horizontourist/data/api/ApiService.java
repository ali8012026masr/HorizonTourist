package com.example.horizontourist.data.api;

import com.example.horizontourist.data.model.AiChatRequest;
import com.example.horizontourist.data.model.AiChatResponse;
import com.example.horizontourist.data.model.Booking;
import com.example.horizontourist.data.model.BookingRequest;
import com.example.horizontourist.data.model.ChangePasswordRequest;
import com.example.horizontourist.data.model.CountResponse;
import com.example.horizontourist.data.model.ForgotPasswordRequest;
import com.example.horizontourist.data.model.Group;
import com.example.horizontourist.data.model.GroupBookingRequest;
import com.example.horizontourist.data.model.GroupCreateRequest;
import com.example.horizontourist.data.model.GroupMember;
import com.example.horizontourist.data.model.GroupMessage;
import com.example.horizontourist.data.model.Guide;
import com.example.horizontourist.data.model.GuideAvailability;
import com.example.horizontourist.data.model.GuideBooking;
import com.example.horizontourist.data.model.GuideBookingRequest;
import com.example.horizontourist.data.model.GuideRating;
import com.example.horizontourist.data.model.GuideRatingRequest;
import com.example.horizontourist.data.model.JoinGroupRequest;
import com.example.horizontourist.data.model.Location;
import com.example.horizontourist.data.model.LoginRequest;
import com.example.horizontourist.data.model.LoginResponse;
import com.example.horizontourist.data.model.MessageRequest;
import com.example.horizontourist.data.model.MessageResponse;
import com.example.horizontourist.data.model.Notification;
import com.example.horizontourist.data.model.Payment;
import com.example.horizontourist.data.model.PaymentRequest;
import com.example.horizontourist.data.model.RegisterTouristRequest;
import com.example.horizontourist.data.model.ResetPasswordRequest;
import com.example.horizontourist.data.model.ServiceRating;
import com.example.horizontourist.data.model.ServiceRatingRequest;
import com.example.horizontourist.data.model.ServiceSlot;
import com.example.horizontourist.data.model.StatusRequest;
import com.example.horizontourist.data.model.Tourist;
import com.example.horizontourist.data.model.UpdateTouristRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // --- Public Auth Endpoints ---

    @POST("auth/register/tourist")
    Call<Tourist> registerTourist(@Body RegisterTouristRequest request);

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/forgot-password")
    Call<MessageResponse> forgotPassword(@Body ForgotPasswordRequest request);

    @POST("auth/reset-password")
    Call<MessageResponse> resetPassword(@Body ResetPasswordRequest request);

    @PUT("auth/change-password")
    Call<MessageResponse> changePassword(@Body ChangePasswordRequest request);

    // --- Slots ---

    @GET("slots/search")
    Call<List<ServiceSlot>> searchSlots(
            @Query("category") String category,
            @Query("origin") String origin,
            @Query("destination") String destination,
            @Query("locationName") String locationName,
            @Query("dateFrom") String dateFrom,
            @Query("dateTo") String dateTo,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice
    );

    @GET("slots/{id}")
    Call<ServiceSlot> getSlotById(@Path("id") Long id);

    // --- Guide Availability ---

    @GET("guide-availability/search")
    Call<List<GuideAvailability>> searchGuideAvailability(
            @Query("dateFrom") String dateFrom,
            @Query("dateTo") String dateTo,
            @Query("location") String location
    );

    @GET("guide-availability/guide/{guideId}")
    Call<List<GuideAvailability>> getGuideAvailabilityByGuideId(@Path("guideId") Long guideId);

    // --- Guides ---

    @GET("guides")
    Call<List<Guide>> getGuides(@Query("available") Boolean available);

    @GET("guides/{id}")
    Call<Guide> getGuideById(@Path("id") Long id);

    @GET("guides/{id}/location")
    Call<Location> getGuideLocation(@Path("id") Long id);

    // --- Ratings ---

    @GET("ratings/guide/{guideId}")
    Call<List<GuideRating>> getGuideRatings(@Path("guideId") Long guideId);

    @POST("ratings/guide")
    Call<GuideRating> rateGuide(@Body GuideRatingRequest request);

    @GET("ratings/provider/{providerId}")
    Call<List<ServiceRating>> getProviderRatings(@Path("providerId") Long providerId);

    @POST("ratings/service")
    Call<ServiceRating> rateService(@Body ServiceRatingRequest request);

    // --- Tourist Profile ---

    @GET("tourists/{id}")
    Call<Tourist> getTouristProfile(@Path("id") Long id);

    @PUT("tourists/{id}")
    Call<Tourist> updateTouristProfile(@Path("id") Long id, @Body UpdateTouristRequest request);

    @DELETE("tourists/{id}")
    Call<Void> deleteTouristAccount(@Path("id") Long id);

    // --- Bookings ---

    @POST("bookings")
    Call<Booking> createBooking(@Body BookingRequest request);

    @GET("bookings/tourist/{touristId}")
    Call<List<Booking>> getTouristBookings(@Path("touristId") Long touristId);

    @PUT("bookings/{id}/status")
    Call<Booking> updateBookingStatus(@Path("id") Long id, @Body StatusRequest request);

    // --- Guide Bookings ---

    @POST("guide-bookings")
    Call<GuideBooking> createGuideBooking(@Body GuideBookingRequest request);

    @GET("guide-bookings/tourist/{touristId}")
    Call<List<GuideBooking>> getTouristGuideBookings(@Path("touristId") Long touristId);

    // --- Payments ---

    @POST("payments")
    Call<Payment> createPayment(@Body PaymentRequest request);

    @GET("payments/{id}")
    Call<Payment> getPaymentById(@Path("id") Long id);

    @GET("payments/booking/{bookingId}")
    Call<List<Payment>> getPaymentsByBookingId(@Path("bookingId") Long bookingId);

    // --- Groups ---

    @POST("groups")
    Call<Group> createGroup(@Body GroupCreateRequest request);

    @POST("groups/join")
    Call<Group> joinGroup(@Body JoinGroupRequest request);

    @GET("groups/{id}")
    Call<Group> getGroupById(@Path("id") Long id);

    @GET("groups/tourist/{touristId}")
    Call<List<Group>> getTouristGroups(@Path("touristId") Long touristId);

    @GET("groups/{id}/members")
    Call<List<GroupMember>> getGroupMembers(@Path("id") Long id);

    @DELETE("groups/{id}/members/{touristId}")
    Call<Void> leaveOrRemoveGroupMember(@Path("id") Long id, @Path("touristId") Long touristId);

    @POST("groups/{id}/booking")
    Call<Group> linkGroupBooking(@Path("id") Long id, @Body GroupBookingRequest request);

    @PUT("groups/{id}/members/{touristId}/paid")
    Call<GroupMember> markGroupMemberPaid(@Path("id") Long id, @Path("touristId") Long touristId);

    @GET("groups/{id}/messages")
    Call<List<GroupMessage>> getGroupMessages(@Path("id") Long id);

    @POST("groups/{id}/messages")
    Call<GroupMessage> sendGroupMessage(@Path("id") Long id, @Body MessageRequest request);

    // --- Notifications ---

    @GET("notifications/user/{userId}")
    Call<List<Notification>> getNotifications(@Path("userId") Long userId);

    @GET("notifications/user/{userId}/unread-count")
    Call<CountResponse> getUnreadNotificationCount(@Path("userId") Long userId);

    @PUT("notifications/{id}/read")
    Call<Notification> markNotificationRead(@Path("id") Long id);

    // --- AI Concierge ---

    @POST("ai/chat")
    Call<AiChatResponse> chatAi(@Body AiChatRequest request);
}
