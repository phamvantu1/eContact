package com.ec.contract.repository;

import com.ec.contract.model.dto.response.StaticCustomerUseContract;
import com.ec.contract.model.entity.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract, Integer> {

    Boolean existsByContractNo(String contractNo);

    @Query(value = "SELECT * FROM contracts c " +
            "WHERE c.status = :status " +
            "and (c.contract_no ILIKE CONCAT('%', :textSearch, '%') OR c.name ILIKE CONCAT('%', :textSearch, '%')) " +
            "AND (:organizationId IS NULL OR c.organization_id = :organizationId ) " +
            "ORDER BY c.created_at DESC",
            countQuery = "SELECT count(*) FROM contracts c " +
                    "WHERE c.status = :status " +
                    "and (c.contract_no ILIKE CONCAT('%', :textSearch, '%') OR c.name ILIKE CONCAT('%', :textSearch, '%')) " +
                    "AND (:organizationId IS NULL OR c.organization_id = :organizationId )",
            nativeQuery = true)
    Page<Contract> findByStatus(
            @Param("status") Integer status,
            @Param("textSearch") String textSearch,
            @Param("organizationId") Integer organizationId,
            Pageable pageable);


    @Query(value = """
            SELECT *
            FROM contracts c
            WHERE c.created_by = :customerId
            AND (
                    :textSearch IS NULL 
                    OR c.contract_no ILIKE CONCAT('%', :textSearch, '%')
                    OR c.name ILIKE CONCAT('%', :textSearch, '%')
                )
            AND (
                    (:status != 1 AND c.status = :status)
                    OR
                    (:status = 1 
                        AND c.status = 20
                        AND c.contract_expire_time <= now() + interval '15 days'
                        AND c.contract_expire_time >= now()
                    )
            )
            AND (:fromDate IS NULL OR c.created_at >= CAST(:fromDate AS timestamp))
            AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp))
            ORDER BY c.created_at DESC
            """,
            countQuery = """
                    SELECT count(*)
                    FROM contracts c
                    WHERE c.created_by = :customerId
                    AND (
                            :textSearch IS NULL 
                            OR c.contract_no ILIKE CONCAT('%', :textSearch, '%')
                            OR c.name ILIKE CONCAT('%', :textSearch, '%')
                        )                        
                    AND (
                            (:status != 1 AND c.status = :status)
                            OR
                            (:status = 1 
                                AND c.status = 20
                                AND c.contract_expire_time <= now() + interval '15 days'
                                AND c.contract_expire_time >= now()
                            )
                    )
                    AND (:fromDate IS NULL OR c.created_at >= CAST(:fromDate AS timestamp))
                    AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp))
                    """,
            nativeQuery = true)
    Page<Contract> findMyContracts(
            @Param("customerId") Integer customerId,
            @Param("status") Integer status,
            @Param("textSearch") String textSearch,
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate,
            Pageable pageable);


    @Query(value = "SELECT distinct c.* from contracts c " +
            "JOIN participants p ON c.id = p.contract_id " +
            "JOIN recipients r ON r.participant_id = p.id " +
            "Where r.email = :email " +
            " and ( (:status = 1 and r.status = 1 and c.status not in(31, 32) ) " +
            " or (:status = 2 and ( r.status = 2 or r.status = 3 or c.status in (31 , 32)) ) )" +
            "AND c.status IN (:listStatus) " +
            "and (c.contract_no ILIKE CONCAT('%', :textSearch, '%') OR c.name ILIKE CONCAT('%', :textSearch, '%')) " +
            "AND (:fromDate IS NULL OR c.created_at >= CAST(:fromDate AS timestamp)) " +
            "AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp)) " +
            "ORDER BY c.created_at DESC",
            countQuery = "SELECT count(*) from contracts c " +
                    "JOIN participants p ON c.id = p.contract_id " +
                    "JOIN recipients r ON r.participant_id = p.id " +
                    "Where r.email = :email " +
                    " and ( (:status = 1 and r.status = 1 and c.status not in(31, 32) ) " +
                    " or (:status = 2 and ( r.status = 2 or r.status = 3 or c.status in (31 , 32)) ) )" +
                    "AND c.status IN (:listStatus) " +
                    "and (c.contract_no ILIKE CONCAT('%', :textSearch, '%') OR c.name ILIKE CONCAT('%', :textSearch, '%')) " +
                    "AND (:fromDate IS NULL OR c.created_at >= CAST(:fromDate AS timestamp)) " +
                    "AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp))"
            , nativeQuery = true)
    Page<Contract> findMyProcessContracts(@Param("email") String email,
                                          @Param("listStatus") List<Integer> listStatus,
                                          @Param("textSearch") String textSearch,
                                          @Param("fromDate") String fromDate,
                                          @Param("toDate") String toDate,
                                          @Param("status") Integer status,
                                          Pageable pageable);


    @Query(value = "SELECT * FROM contracts c " +
            "WHERE c.created_by in (:listCustomerIds) " +
            "AND c.status = :status " +
            "and (c.contract_no ILIKE CONCAT('%', :textSearch, '%') OR c.name ILIKE CONCAT('%', :textSearch, '%')) " +
            "AND (:fromDate IS NULL OR c.created_at >= CAST(:fromDate AS timestamp)) " +
            "AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp)) " +
            "ORDER BY c.created_at DESC",
            countQuery = "SELECT count(*) FROM contracts c " +
                    "WHERE c.created_by in (:listCustomerIds) " +
                    "AND c.status = :status " +
                    "and (c.contract_no ILIKE CONCAT('%', :textSearch, '%') OR c.name ILIKE CONCAT('%', :textSearch, '%')) " +
                    "AND (:fromDate IS NULL  OR c.created_at >= CAST(:fromDate AS timestamp)) " +
                    "AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp))",
            nativeQuery = true)
    Page<Contract> findContractsByOrganization(@Param("listCustomerIds") List<Integer> listCustomerIds,
                                               @Param("status") Integer status,
                                               @Param("textSearch") String textSearch,
                                               @Param("fromDate") String fromDate,
                                               @Param("toDate") String toDate,
                                               Pageable pageable);


    /**
     * Lấy hợp đồng theo recipientId
     *
     * @return
     */
    @Query(
            value = " select c.* from contracts c " +
                    " join participants p on c.id = p.contract_id " +
                    " join recipients r on p.id = r.participant_id " +
                    " where r.id = :recipientId " +
                    "limit 1 ",
            nativeQuery = true)
    Optional<Contract> findByRecipientId(@Param("recipientId") Integer recipientId);

    @Query(value = "SELECT count(distinct c.id) from contracts c " +
            " JOIN participants p ON c.id = p.contract_id " +
            " JOIN recipients r ON r.participant_id = p.id " +
            " where r.email = :email " +
            " and ( (:status = 30 and c.status = 30)  " +
            " or (:status = 20 and c.status = 20 and r.status = 1) " +
            " or (:status = 50 and r.status not in (1, 2) )" + // fix cung 50 la waiting
            " or (:status = 1 and c.status = 20  and c.contract_expire_time <= now() + interval '15 days' and c.contract_expire_time >= now() )" +
            " )  "
            , nativeQuery = true)
    Integer countMyProcessContract(@Param("email") String email,
                                   @Param("status") Integer status);

    @Query(value = "SELECT count(distinct c.id) from contracts c " +
            "where c.created_by = :customerId " +
            "and ( (:status = 30 and c.status = 30 )" +
            " or (:status = 20 and c.status = 20) " +
            " or (:status = 31 and c.status = 31) " +
            " or (:status = 32 and c.status = 32) " +
            " or (:status = 2 and c.status = 2) " +
            " )  " +
            "and (:fromDate IS NULL OR c.created_at >= CAST(:fromDate AS timestamp)) " +
            "AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp)) "
            , nativeQuery = true)
    Integer countMyContractByStatus(@Param("customerId") Integer customerId,
                                    @Param("fromDate") String fromDate,
                                    @Param("toDate") String toDate,
                                    @Param("status") Integer status);

    @Query(value = "SELECT count(distinct c.id) from contracts c " +
            "where ( organizationId is null or c.organization_id = :organizationId )" +
            "and c.status = 30 " +
            "and (:fromDate IS NULL OR c.created_at >= CAST(:fromDate AS timestamp)) " +
            "AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp)) "
            , nativeQuery = true)
    Integer countTotalSignedContractsByOrganization(@Param("organizationId") Integer organizationId,
                                                    @Param("fromDate") String fromDate,
                                                    @Param("toDate") String toDate);

    @Query(value = "SELECT count(distinct c.id) from contracts c " +
            "where ( organizationId is null or c.organization_id = :organizationId )" +
            "and c.status = 20 " +
            "and (:fromDate IS NULL OR c.created_at >= CAST(:fromDate AS timestamp)) " +
            "AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp)) "
            , nativeQuery = true)
    Integer countTotalProcessingContractsByOrganization(@Param("organizationId") Integer organizationId,
                                                        @Param("fromDate") String fromDate,
                                                        @Param("toDate") String toDate);

    @Query(value = "SELECT count(distinct c.id) from contracts c " +
            "where ( organizationId is null or c.organization_id = :organizationId )" +
            "and c.status = 31 " +
            "and (:fromDate IS NULL OR c.created_at >= CAST(:fromDate AS timestamp)) " +
            "AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp)) "
            , nativeQuery = true)
    Integer countTotalRejectedContractsByOrganization(@Param("organizationId") Integer organizationId,
                                                      @Param("fromDate") String fromDate,
                                                      @Param("toDate") String toDate);

    @Query(value = " SELECT count(distinct c.id) from contracts c " +
            "where ( organizationId is null or c.organization_id = :organizationId )" +
            "and c.status = 32 " +
            "and (:fromDate IS NULL OR c.created_at >= CAST(:fromDate AS timestamp)) " +
            "AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp)) "
            , nativeQuery = true)
    Integer countTotalCancelledContractsByOrganization(@Param("organizationId") Integer organizationId,
                                                       @Param("fromDate") String fromDate,
                                                       @Param("toDate") String toDate);

    @Query(value = " SELECT count(distinct c.id) from contracts c " +
            "where ( organizationId is null or c.organization_id = :organizationId )" +
            "and c.status = 2 " +
            "and (:fromDate IS NULL OR c.created_at >= CAST(:fromDate AS timestamp)) " +
            "AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp)) "
            , nativeQuery = true)
    Integer countTotalExpiredContractsByOrganization(@Param("organizationId") Integer organizationId,
                                                     @Param("fromDate") String fromDate,
                                                     @Param("toDate") String toDate);

    @Query(value = "SELECT c.* from contracts c " +
            "where c.organization_id = :organizationId " +
            "and (:fromDate IS NULL OR c.created_at >= CAST(:fromDate AS timestamp)) " +
            "AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp)) " +
            "AND (:completedFromDate IS NULL OR (c.updated_at >= CAST(:completedFromDate AS timestamp) and c.status = 30) ) " +
            "AND (:completedToDate IS NULL OR (c.updated_at <= CAST(:completedToDate AS timestamp) and c.status = 30)) " +
            "AND (:status IS NULL OR c.status = :status ) " +
            "and (:textSearch IS NULL OR c.contract_no ILIKE CONCAT('%', :textSearch, '%') OR c.name ILIKE CONCAT('%', :textSearch, '%')) " +
            "ORDER BY c.created_at DESC",
            countQuery = "SELECT count(*) from contracts c " +
                    "where c.organization_id = :organizationId " +
                    "and (:fromDate IS NULL OR c.created_at >= CAST(:fromDate AS timestamp)) " +
                    "AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp)) " +
                    "AND (:completedFromDate IS NULL OR (c.updated_at >= CAST(:completedFromDate AS timestamp) and c.status = 30) ) " +
                    "AND (:completedToDate IS NULL OR (c.updated_at <= CAST(:completedToDate AS timestamp) and c.status = 30)) " +
                    "AND (:status IS NULL OR c.status = :status ) " +
                    "and (:textSearch IS NULL OR c.contract_no ILIKE CONCAT('%', :textSearch, '%') OR c.name ILIKE CONCAT('%', :textSearch, '%')) "
            , nativeQuery = true)
    Page<Contract> reportDetail(@Param("organizationId") int organizationId,
                                @Param("fromDate") String fromDate,
                                @Param("toDate") String toDate,
                                @Param("completedFromDate") String completedFromDate,
                                @Param("completedToDate") String completedToDate,
                                @Param("status") Integer status,
                                @Param("textSearch") String textSearch,
                                Pageable pageable);

    @Query(value = "SELECT c.* from contracts c " +
            "where c.organization_id = :organizationId " +
            "and (:fromDate IS NULL OR c.created_at >= CAST(:fromDate AS timestamp)) " +
            "AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp)) " +
            "AND (:completedFromDate IS NULL OR (c.updated_at >= CAST(:completedFromDate AS timestamp) and c.status = 30) ) " +
            "AND (:completedToDate IS NULL OR (c.updated_at <= CAST(:completedToDate AS timestamp) and c.status = 30)) " +
            "AND (:status IS NULL OR c.status = :status ) " +
            "and (c.contract_no ILIKE CONCAT('%', :textSearch, '%') OR c.name ILIKE CONCAT('%', :textSearch, '%')) " +
            "ORDER BY c.created_at DESC",
            countQuery = "SELECT count(*) from contracts c " +
                    "where c.organization_id = :organizationId " +
                    "and (:fromDate IS NULL OR c.created_at >= CAST(:fromDate AS timestamp)) " +
                    "AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp)) " +
                    "AND (:completedFromDate IS NULL OR (c.updated_at >= CAST(:completedFromDate AS timestamp) and c.status = 30) ) " +
                    "AND (:completedToDate IS NULL OR (c.updated_at <= CAST(:completedToDate AS timestamp) and c.status = 30)) " +
                    "AND (:status IS NULL OR c.status = :status ) " +
                    "and (c.contract_no ILIKE CONCAT('%', :textSearch, '%') OR c.name ILIKE CONCAT('%', :textSearch, '%')) "
            , nativeQuery = true)
    Page<Contract> reportByStatus(@Param("organizationId") int organizationId,
                                  @Param("fromDate") String fromDate,
                                  @Param("toDate") String toDate,
                                  @Param("completedFromDate") String completedFromDate,
                                  @Param("completedToDate") String completedToDate,
                                  @Param("status") Integer status,
                                  @Param("textSearch") String textSearch,
                                  Pageable pageable);

    @Query(value = "SELECT distinct c.* from contracts c " +
            "JOIN participants p ON c.id = p.contract_id " +
            "JOIN recipients r ON r.participant_id = p.id " +
            "where r.email IN (:emails) " +
            "and (:fromDate IS NULL OR c.created_at >= CAST(:fromDate AS timestamp)) " +
            "AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp)) " +
            "AND (:completedFromDate IS NULL OR (c.updated_at >= CAST(:completedFromDate AS timestamp) and c.status = 30) ) " +
            "AND (:completedToDate IS NULL OR (c.updated_at <= CAST(:completedToDate AS timestamp) and c.status = 30)) " +
            "AND (:status IS NULL OR c.status = :status ) " +
            "and (c.contract_no ILIKE CONCAT('%', :textSearch, '%') OR c.name ILIKE CONCAT('%', :textSearch, '%')) " +
            "ORDER BY c.created_at DESC",
            countQuery = "SELECT count(*) from contracts c " +
                    "JOIN participants p ON c.id = p.contract_id " +
                    "JOIN recipients r ON r.participant_id = p.id " +
                    "where r.email IN (:emails) " +
                    "and (:fromDate IS NULL OR c.created_at >= CAST(:fromDate AS timestamp)) " +
                    "AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp)) " +
                    "AND (:completedFromDate IS NULL OR (c.updated_at >= CAST(:completedFromDate AS timestamp) and c.status = 30) ) " +
                    "AND (:completedToDate IS NULL OR (c.updated_at <= CAST(:completedToDate AS timestamp) and c.status = 30)) " +
                    "AND (:status IS NULL OR c.status = :status ) " +
                    "and (c.contract_no ILIKE CONCAT('%', :textSearch, '%') OR c.name ILIKE CONCAT('%', :textSearch, '%')) "
            , nativeQuery = true)
    Page<Contract> reportMyProcess(@Param("emails") List<String> emails,
                                   @Param("fromDate") String fromDate,
                                   @Param("toDate") String toDate,
                                   @Param("completedFromDate") String completedFromDate,
                                   @Param("completedToDate") String completedToDate,
                                   @Param("status") Integer status,
                                   @Param("textSearch") String textSearch,
                                   Pageable pageable);

    @Query(value = "SELECT count(distinct c.id) from contracts c " +
            "where c.organization_id = :organizationId " +
            "and (c.status = :status or (:status = 1 and c.contract_expire_time <= now() + interval '15 days' and c.contract_expire_time >= now() ) ) " +
            "and (:fromDate IS NULL OR c.created_at >= CAST(:fromDate AS timestamp)) " +
            "AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp)) "
            , nativeQuery = true)
    Integer countContractByStatus(@Param("organizationId") int organizationId,
                                  @Param("fromDate") String fromDate,
                                  @Param("toDate") String toDate,
                                  @Param("status") Integer status);

    @Query(value = "SELECT count(distinct c.id) from contracts c " +
            "where c.organization_id = :organizationId " +
            "and (:typeId IS NULL OR c.type_id = :typeId ) " +
            "and ( c.status = :status or (:status = 1 and c.contract_expire_time <= now() + interval '15 days' and c.contract_expire_time >= now() ) ) " +
            "and (:fromDate IS NULL OR c.created_at >= CAST(:fromDate AS timestamp)) " +
            "AND (:toDate IS NULL OR c.created_at <= CAST(:toDate AS timestamp)) "
            , nativeQuery = true)
    Integer countContractByType(@Param("organizationId") int organizationId,
                                @Param("fromDate") String fromDate,
                                @Param("toDate") String toDate,
                                @Param("typeId") Integer typeId,
                                @Param("status") Integer status);

    @Query(value = "SELECT c.customer_id AS customerId," +
            "count(*) as total " +
            "from contracts c " +
            "group by customer_id " +
            "order by total desc " +
            "limit 10 "
            , nativeQuery = true)
    List<StaticCustomerUseContract> statisticsCustomerUseMaxContracts();

    @Query(value = "SELECT c.* from contracts c " +
            "where c.status = 20 " +
            "and c.contract_expire_time <= :currentDate "
            , nativeQuery = true)
    List<Contract> findContractsToExpire(@Param("currentDate") LocalDateTime currentDate);

    @Query(value = "SELECT c.* from contracts c " +
            "where c.status = 20 " +
            "and c.contract_expire_time <= now() + interval '15 days' and c.contract_expire_time >= now() "
    , nativeQuery = true)
    List<Contract> getContractsAboutToExpire();
}
