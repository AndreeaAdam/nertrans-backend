package ro.nertrans.filters.filteredServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import ro.nertrans.config.UserRoleEnum;
import ro.nertrans.filters.filteredDTOS.PaymentDocumentSearchDTO;
import ro.nertrans.models.PaymentDocument;
import ro.nertrans.models.User;
import ro.nertrans.repositories.PaymentDocumentRepository;
import ro.nertrans.services.UserService;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentDocSearchService {
    @Inject
    private MongoTemplate mongoTemplate;
    @Autowired
    private PaymentDocumentRepository paymentDocumentRepository;
    @Autowired
    private UserService userService;

    public Page<PaymentDocument> listPaymentDocsFiltered(PaymentDocumentSearchDTO paymentDocumentSearchDTO, int page, int size, String sort, String dir, HttpServletRequest request) {
        Pageable pageable;
        if (size == -1 && paymentDocumentRepository.findAll().size() > 0) {
            pageable = PageRequest.of(page, paymentDocumentRepository.findAll().size());
        } else {
            pageable = PageRequest.of(page, size);
        }
        Query dynamicQuery;
        /**
         * @Improvable
         */
        if (dir != null && sort != null && (dir.equals("asc") || dir.equals("desc"))) {
            if (dir.equals("asc")) {
                dynamicQuery = new Query().with(pageable).with(Sort.by(Sort.Direction.ASC, sort));
            } else {
                dynamicQuery = new Query().with(pageable).with(Sort.by(Sort.Direction.DESC, sort));
            }
        } else {
            dynamicQuery = new Query().with(pageable);
        }
        Optional<User> user = userService.getCurrentUser(request);
        Criteria docSeriesCriteria;
        if (user.get().getRoles().contains(UserRoleEnum.ROLE_super_admin) || user.get().isActLikeAdmin()) {
            if (paymentDocumentSearchDTO.getDocSeries() != null) {
                docSeriesCriteria = Criteria.where("docSeries").is(paymentDocumentSearchDTO.getDocSeries());
                dynamicQuery.addCriteria(docSeriesCriteria);
            }
        } else {
            docSeriesCriteria = Criteria.where("docSeries").is(user.get().getOffice());
            dynamicQuery.addCriteria(docSeriesCriteria);
        }
        if (paymentDocumentSearchDTO.getName() != null) {
            Criteria paymentDocNameCriteria = Criteria.where("name").regex(paymentDocumentSearchDTO.getName(), "i");
            dynamicQuery.addCriteria(paymentDocNameCriteria);
        }
        if (paymentDocumentSearchDTO.getCurrency() != null) {
            Criteria paymentCurrencyCriteria = Criteria.where("currency").is(paymentDocumentSearchDTO.getCurrency());
            dynamicQuery.addCriteria(paymentCurrencyCriteria);
        }
        if (paymentDocumentSearchDTO.getUserId() != null) {
            Criteria paymentDocUserIdCriteria = Criteria.where("userId").is(paymentDocumentSearchDTO.getUserId());
            dynamicQuery.addCriteria(paymentDocUserIdCriteria);
        }
        if (paymentDocumentSearchDTO.getPaymentMethod() != null) {
            Criteria paymentMethodCriteria = Criteria.where("paymentMethod").is(paymentDocumentSearchDTO.getPaymentMethod());
            dynamicQuery.addCriteria(paymentMethodCriteria);
        }
        if (paymentDocumentSearchDTO.getPartnerName() != null) {
            Criteria partnerNameCriteria = Criteria.where("partnerName").regex(paymentDocumentSearchDTO.getPartnerName(), "i");
            dynamicQuery.addCriteria(partnerNameCriteria);
        }
        if (paymentDocumentSearchDTO.getLocalReferenceNumber() != null) {
            Criteria LRNCriteria = Criteria.where("localReferenceNumber").regex(paymentDocumentSearchDTO.getLocalReferenceNumber(), "i");
            dynamicQuery.addCriteria(LRNCriteria);
        }
        if (paymentDocumentSearchDTO.getStatus() != null) {
            Criteria statusCriteria = Criteria.where("status").is(paymentDocumentSearchDTO.getStatus());
            dynamicQuery.addCriteria(statusCriteria);
        }
        if (paymentDocumentSearchDTO.getStartDate() != null && paymentDocumentSearchDTO.getEndDate() != null) {
            Criteria dateRangeCriteria = Criteria.where("date").gte(LocalDate.parse(paymentDocumentSearchDTO.getStartDate()).atStartOfDay()).lte(LocalDate.parse(paymentDocumentSearchDTO.getEndDate()).atTime(23,59));
            dynamicQuery.addCriteria(dateRangeCriteria);
        }

        dynamicQuery.collation(Collation.of("en").
                strength(Collation.ComparisonLevel.secondary()));
        List<PaymentDocument> list = mongoTemplate.find(dynamicQuery, PaymentDocument.class);

        return PageableExecutionUtils.getPage(
                list,
                pageable,
                () -> mongoTemplate.count(Query.of(dynamicQuery).limit(-1).skip(-1), PaymentDocument.class));
    }
}
