package com.example.horizontourist.data.model;

public final class Constants {

    private Constants() {
    }

    // Roles
    public static final String ROLE_TOURIST = "TOURIST";

    // BookingStatus
    public static final String BOOKING_STATUS_PENDING = "PENDING";
    public static final String BOOKING_STATUS_CONFIRMED = "CONFIRMED";
    public static final String BOOKING_STATUS_CANCELLED = "CANCELLED";
    public static final String BOOKING_STATUS_COMPLETED = "COMPLETED";

    // PaymentStatus (booking.paymentStatus)
    public static final String PAYMENT_STATUS_UNPAID = "UNPAID";
    public static final String PAYMENT_STATUS_PAID = "PAID";
    public static final String PAYMENT_STATUS_REFUNDED = "REFUNDED";

    // PaymentTxStatus
    public static final String TX_STATUS_PENDING = "PENDING";
    public static final String TX_STATUS_SUCCESS = "SUCCESS";
    public static final String TX_STATUS_FAILED = "FAILED";
    public static final String TX_STATUS_REFUNDED = "REFUNDED";

    // PaymentMethod
    public static final String PAYMENT_METHOD_CARD = "CARD";
    public static final String PAYMENT_METHOD_MOBILE_BANKING = "MOBILE_BANKING";
    public static final String PAYMENT_METHOD_CASH = "CASH";

    // GuideBookingStatus
    public static final String GUIDE_BOOKING_STATUS_REQUESTED = "REQUESTED";
    public static final String GUIDE_BOOKING_STATUS_ACCEPTED = "ACCEPTED";
    public static final String GUIDE_BOOKING_STATUS_DECLINED = "DECLINED";
    public static final String GUIDE_BOOKING_STATUS_COMPLETED = "COMPLETED";

    // ServiceCategory
    public static final String CATEGORY_BUS = "BUS";
    public static final String CATEGORY_MICROBUS = "MICROBUS";
    public static final String CATEGORY_LAUNCH = "LAUNCH";
    public static final String CATEGORY_TRAIN = "TRAIN";
    public static final String CATEGORY_AIRPLANE = "AIRPLANE";
    public static final String CATEGORY_SHIP = "SHIP";
    public static final String CATEGORY_HOTEL = "HOTEL";
    public static final String CATEGORY_RESORT = "RESORT";
    public static final String CATEGORY_CONVENTION_CENTER = "CONVENTION_CENTER";
    public static final String CATEGORY_BUFFET = "BUFFET";
    public static final String CATEGORY_AMUSEMENT_PARK = "AMUSEMENT_PARK";

    // SlotStatus
    public static final String SLOT_STATUS_OPEN = "OPEN";
    public static final String SLOT_STATUS_FULL = "FULL";
    public static final String SLOT_STATUS_CLOSED = "CLOSED";
    public static final String SLOT_STATUS_CANCELLED = "CANCELLED";

    // GroupMemberStatus
    public static final String GROUP_MEMBER_STATUS_INVITED = "INVITED";
    public static final String GROUP_MEMBER_STATUS_JOINED = "JOINED";
    public static final String GROUP_MEMBER_STATUS_DECLINED = "DECLINED";

    // UserStatus
    public static final String USER_STATUS_ACTIVE = "ACTIVE";
    public static final String USER_STATUS_PENDING_VERIFICATION = "PENDING_VERIFICATION";
    public static final String USER_STATUS_SUSPENDED = "SUSPENDED";
    public static final String USER_STATUS_REVOKED = "REVOKED";

    // VerificationStatus
    public static final String VERIFICATION_STATUS_VERIFIED = "VERIFIED";
    public static final String VERIFICATION_STATUS_PENDING = "PENDING";
    public static final String VERIFICATION_STATUS_REJECTED = "REJECTED";

    // AvailabilityStatus
    public static final String AVAILABILITY_STATUS_OPEN = "OPEN";
    public static final String AVAILABILITY_STATUS_BOOKED = "BOOKED";
    public static final String AVAILABILITY_STATUS_CANCELLED = "CANCELLED";

    // AI Action Types
    public static final String AI_ACTION_FILTER_SLOTS = "FILTER_SLOTS";
    public static final String AI_ACTION_RECOMMEND_BUNDLE = "RECOMMEND_BUNDLE";
    public static final String AI_ACTION_RECOMMEND_GROUP_BUNDLE = "RECOMMEND_GROUP_BUNDLE";
    public static final String AI_ACTION_NONE = "NONE";
}
