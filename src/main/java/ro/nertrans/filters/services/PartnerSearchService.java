package ro.nertrans.filters.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import ro.nertrans.filters.dtos.PartnerSearchDTO;
import ro.nertrans.models.Partner;
import ro.nertrans.repositories.PartnerRepository;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
public class PartnerSearchService {
    @Inject
    private MongoTemplate mongoTemplate;
    @Autowired
    private PartnerRepository partnerRepository;

    /**
     * @param partnerSearchDTO PartnerSearchDTO
     * @param page - current Page ( starts with 0 )
     * @param size -
     * @param sort - what to sort by . ex : email - sorts by email
     * @param dir  - asc / desc
     * @return Page<User>
     * @Description: Filtered and paginated list of partners
     */
    public Page<Partner> listPartnerFiltered(PartnerSearchDTO partnerSearchDTO, int page, int size, String sort, String dir, HttpServletRequest request) {
        Pageable pageable;
        if (size == -1 && !partnerRepository.findAll().isEmpty()) {
            pageable = PageRequest.of(page, partnerRepository.findAll().size());
        }else  pageable = PageRequest.of(page, size);
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
        if (partnerSearchDTO.getName() != null) {
            Criteria partnerNameCriteria = Criteria.where("name").regex(partnerSearchDTO.getName(), "i");
            dynamicQuery.addCriteria(partnerNameCriteria);
        }
        if (partnerSearchDTO.getCUI() != null) {
            Criteria partnerRegistrationCodeCriteria = Criteria.where("CUI").regex(partnerSearchDTO.getCUI(), "i");
            dynamicQuery.addCriteria(partnerRegistrationCodeCriteria);
        }
        if (partnerSearchDTO.getAddress() != null) {
            Criteria partnerAddressCriteria = Criteria.where("address").in(partnerSearchDTO.getAddress(), "i");
            dynamicQuery.addCriteria(partnerAddressCriteria);
        }
        if (partnerSearchDTO.getUserId() != null) {
            Criteria partnerUserIdCriteria = Criteria.where("userId").is(partnerSearchDTO.getUserId());
            dynamicQuery.addCriteria(partnerUserIdCriteria);
        }
        if (partnerSearchDTO.getEmail() != null) {
            Criteria partnerEmailCriteria = Criteria.where("email").regex(partnerSearchDTO.getEmail(), "i");
            dynamicQuery.addCriteria(partnerEmailCriteria);
        }
        if (partnerSearchDTO.getCIF() != null) {
            Criteria partnerRegistrationCodeCriteria = Criteria.where("CIF").regex(partnerSearchDTO.getCIF(), "i");
            dynamicQuery.addCriteria(partnerRegistrationCodeCriteria);
        }
        dynamicQuery.collation(Collation.of("en").
                strength(Collation.ComparisonLevel.secondary()));
        List<Partner> list = mongoTemplate.find(dynamicQuery, Partner.class);
        return org.springframework.data.support.PageableExecutionUtils.getPage(list, pageable, () -> mongoTemplate.count(Query.of(dynamicQuery).limit(-1).skip(-1), Partner.class));
    }
}
