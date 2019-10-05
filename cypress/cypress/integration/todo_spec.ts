/// <reference types="Cypress" />

const title = 'Buy beers';

describe('Normal case', function () {

    beforeEach(() => {
        Cypress.Cookies.preserveOnce('Bearer');
    })

    it('Login with sucessful credentials should land on dashboard.', () => {
        cy.visit('http://localhost:4200');

        cy.get('input[formcontrolname=login]')
            .type('cyril');

        cy.get('input[formcontrolname=password]')
            .type('balit');

        cy.get('button[type=submit]')
            .click()

        cy.url().should('include', '/dashboard');
    });

    it('Goto todo creation page', () => {

        cy.get('.dashboard__new-todo').click();

        cy.url().should('include', '/create');

    });

    it('Creating todo should bring back to dashboard with new todo in list', () => {

        cy.get('input[formcontrolname=title]')
            .type(title);

        cy.get('textarea[formcontrolname=text]')
            .type('50 litres');

        cy.get('button[type=submit]')
            .click();

        cy.url().should('include', '/display');

        cy.get('a[href=\\/dashboard]')
            .click();

        cy.url().should('include', '/dashboard');

        cy.get('tr').contains('td:nth-child(2)', title);

    });
})
