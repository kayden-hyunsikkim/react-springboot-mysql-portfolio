import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './portfolio.reducer';

export const PortfolioDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const portfolioEntity = useAppSelector(state => state.portfolio.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="portfolioDetailsHeading">
          <Translate contentKey="portfolioApp.portfolio.detail.title">Portfolio</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{portfolioEntity.id}</dd>
          <dt>
            <span id="projectName">
              <Translate contentKey="portfolioApp.portfolio.projectName">Project Name</Translate>
            </span>
          </dt>
          <dd>{portfolioEntity.projectName}</dd>
          <dt>
            <span id="description">
              <Translate contentKey="portfolioApp.portfolio.description">Description</Translate>
            </span>
          </dt>
          <dd>{portfolioEntity.description}</dd>
          <dt>
            <span id="imageUrl">
              <Translate contentKey="portfolioApp.portfolio.imageUrl">Image Url</Translate>
            </span>
          </dt>
          <dd>{portfolioEntity.imageUrl}</dd>
          <dt>
            <span id="link">
              <Translate contentKey="portfolioApp.portfolio.link">Link</Translate>
            </span>
          </dt>
          <dd>{portfolioEntity.link}</dd>
          <dt>
            <Translate contentKey="portfolioApp.portfolio.user">User</Translate>
          </dt>
          <dd>{portfolioEntity.user ? portfolioEntity.user.login : ''}</dd>
        </dl>
        <Button tag={Link} to="/portfolio" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/portfolio/${portfolioEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default PortfolioDetail;
