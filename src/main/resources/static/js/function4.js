const API = {
    contact: '/api/contact-messages',
    testimonials: '/api/testimonials',
    approved: '/api/testimonials/approved',
    adminTestimonials: '/api/admin/testimonials',
    adminContacts: '/api/admin/contact-messages'
};


// --------------------------------------------------
// Helper functions
// --------------------------------------------------

const $ = (selector, root = document) =>
    root.querySelector(selector);

const $$ = (selector, root = document) =>
    [...root.querySelectorAll(selector)];


const esc = value =>
    String(value ?? '').replace(
        /[&<>"']/g,
        character => ({
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        })[character]
    );


const initials = name =>
    (name || 'TJ')
        .split(/\s+/)
        .slice(0, 2)
        .map(word => word[0] || '')
        .join('')
        .toUpperCase();


// --------------------------------------------------
// REST API helper
// --------------------------------------------------

async function req(url, options = {}) {

    const response = await fetch(url, {
        headers: {
            'Content-Type': 'application/json'
        },
        ...options
    });

    const text = await response.text();

    if (!response.ok) {
        throw new Error(
            text ||
            `Request failed with status ${response.status}`
        );
    }

    return text
        ? JSON.parse(text)
        : null;
}


// --------------------------------------------------
// Notification
// --------------------------------------------------

function notice(element, message, type = 'success') {

    if (!element) {
        return;
    }

    element.textContent = message;
    element.className = `notice show ${type}`;
}


// --------------------------------------------------
// PUBLIC TESTIMONIALS
// --------------------------------------------------

function renderPublic(items) {

    const box = $('#testimonialCards');

    if (!box) {
        return;
    }

    if (!items || items.length === 0) {

        box.innerHTML = `
            <p class="empty-message">
                No approved testimonials available yet.
            </p>
        `;

        return;
    }

    const testimonials = items.slice(0, 3);

    box.innerHTML = testimonials
        .map(testimonial => `

            <article class="testimonial-card">

                <div class="profile">

                    <div class="avatar">
                        ${initials(testimonial.name)}
                    </div>

                    <div>

                        <strong>
                            ${esc(testimonial.name)}
                        </strong>

                        <span>
                            ${esc(
            testimonial.country ||
            'International Customer'
        )}
                        </span>

                    </div>

                </div>


                <div class="stars">
                    ${'★'.repeat(testimonial.rating || 5)}
                </div>


                <div class="quote">
                    “${esc(testimonial.message)}”
                </div>

            </article>

        `)
        .join('');
}


async function loadPublic() {

    try {

        const testimonials =
            await req(API.approved);

        renderPublic(testimonials);

    } catch (error) {

        console.error(
            'Unable to load approved testimonials:',
            error
        );

        renderPublic([]);
    }
}


// --------------------------------------------------
// CONTACT FORM
// --------------------------------------------------

function contactForm() {

    const form = $('#contactForm');
    const noticeBox = $('#contactNotice');

    if (!form) {
        return;
    }


    form.addEventListener(
        'submit',
        async event => {

            event.preventDefault();


            const data =
                Object.fromEntries(
                    new FormData(form)
                );


            if (
                !data.name ||
                !data.email ||
                !data.subject ||
                !data.message
            ) {

                notice(
                    noticeBox,
                    'Please complete all required fields.',
                    'error'
                );

                return;
            }


            try {

                await req(
                    API.contact,
                    {
                        method: 'POST',
                        body: JSON.stringify(data)
                    }
                );


                form.reset();


                notice(
                    noticeBox,
                    'Thank you. Your message has been sent.'
                );


            } catch (error) {

                console.error(
                    'Contact message submission failed:',
                    error
                );


                notice(
                    noticeBox,
                    'Unable to send your message. Please try again.',
                    'error'
                );
            }
        }
    );
}


// --------------------------------------------------
// TESTIMONIAL FORM
// --------------------------------------------------

function testimonialForm() {

    const form = $('#testimonialForm');
    const noticeBox = $('#testimonialNotice');

    if (!form) {
        return;
    }


    form.addEventListener(
        'submit',
        async event => {

            event.preventDefault();


            const data =
                Object.fromEntries(
                    new FormData(form)
                );


            data.rating =
                Number(data.rating || 0);


            if (
                !data.name ||
                !data.country ||
                data.rating < 1 ||
                !data.message
            ) {

                notice(
                    noticeBox,
                    'Complete all fields and select a rating.',
                    'error'
                );

                return;
            }


            if (
                data.message.trim().length < 10
            ) {

                notice(
                    noticeBox,
                    'Testimonial must contain at least 10 characters.',
                    'error'
                );

                return;
            }


            try {

                await req(
                    API.testimonials,
                    {
                        method: 'POST',
                        body: JSON.stringify({
                            name: data.name.trim(),
                            country: data.country.trim(),
                            rating: data.rating,
                            message: data.message.trim()
                        })
                    }
                );


                form.reset();


                notice(
                    noticeBox,
                    'Testimonial submitted successfully and is pending admin approval.'
                );


            } catch (error) {

                console.error(
                    'Testimonial submission failed:',
                    error
                );


                notice(
                    noticeBox,
                    'Unable to submit testimonial. Please try again.',
                    'error'
                );
            }
        }
    );
}


// --------------------------------------------------
// ADMIN TESTIMONIAL MANAGEMENT
// --------------------------------------------------

let admin = [];


function renderAdmin(filter = 'ALL') {

    const tableBody =
        $('#testimonialTableBody');

    if (!tableBody) {
        return;
    }


    const list =
        admin.filter(
            testimonial =>
                filter === 'ALL' ||
                testimonial.status === filter
        );


    tableBody.innerHTML =
        list.map(testimonial => `

            <tr>

                <td>
                    <b>
                        ${esc(testimonial.name)}
                    </b>
                </td>

                <td>
                    ${esc(testimonial.country)}
                </td>

                <td>

                    <span
                        class="stars"
                        style="font-size:13px"
                    >
                        ${'★'.repeat(testimonial.rating)}
                    </span>

                </td>

                <td>
                    ${esc(testimonial.message)}
                </td>

                <td>
                    ${esc(
            (testimonial.createdAt || '')
                .slice(0, 10)
        )}
                </td>

                <td>

                    <span
                        class="status ${testimonial.status.toLowerCase()}"
                    >
                        ${testimonial.status}
                    </span>

                </td>

                <td>

                    <div class="row-actions">

                        <button
                            class="btn btn-success btn-sm"
                            onclick="
                                updateStatus(
                                    ${testimonial.testimonialId},
                                    'APPROVED'
                                )
                            "
                        >
                            ✓
                        </button>


                        <button
                            class="btn btn-danger btn-sm"
                            onclick="
                                updateStatus(
                                    ${testimonial.testimonialId},
                                    'REJECTED'
                                )
                            "
                        >
                            ✕
                        </button>

                    </div>

                </td>

            </tr>

        `).join('') ||

        `
            <tr>
                <td colspan="7">
                    No testimonials found.
                </td>
            </tr>
        `;
}


// --------------------------------------------------
// LOAD ADMIN TESTIMONIALS
// --------------------------------------------------

async function loadAdmin() {

    try {

        admin =
            await req(
                API.adminTestimonials
            );

    } catch (error) {

        console.error(
            'Unable to load admin testimonials:',
            error
        );

        admin = [];
    }


    renderAdmin();

    counts();
}


// --------------------------------------------------
// APPROVE / REJECT
// --------------------------------------------------

async function updateStatus(id, status) {

    const action =
        status === 'APPROVED'
            ? 'approve'
            : 'reject';


    const endpoint =
        `${API.adminTestimonials}/${id}/${action}`;


    try {

        const updated =
            await req(
                endpoint,
                {
                    method: 'PATCH'
                }
            );


        const index =
            admin.findIndex(
                testimonial =>
                    Number(
                        testimonial.testimonialId
                    ) === Number(id)
            );


        if (index !== -1) {

            admin[index] = updated;
        }


        const activeFilter =
            $('.tab.active')
                ?.dataset.filter || 'ALL';


        renderAdmin(activeFilter);

        counts();


    } catch (error) {

        console.error(
            'Unable to update testimonial:',
            error
        );

        alert(
            'Unable to update testimonial. Admin authentication may be required.'
        );
    }
}


window.updateStatus =
    updateStatus;


// --------------------------------------------------
// ADMIN COUNTERS
// --------------------------------------------------

function counts() {

    const totals = {

        ALL:
        admin.length,

        PENDING:
        admin.filter(
            testimonial =>
                testimonial.status === 'PENDING'
        ).length,

        APPROVED:
        admin.filter(
            testimonial =>
                testimonial.status === 'APPROVED'
        ).length,

        REJECTED:
        admin.filter(
            testimonial =>
                testimonial.status === 'REJECTED'
        ).length
    };


    $$('.tab').forEach(tab => {

        tab.textContent =
            `${tab.dataset.label} (${totals[tab.dataset.filter] || 0})`;
    });
}


// --------------------------------------------------
// ADMIN FILTER TABS
// --------------------------------------------------

function tabs() {

    $$('.tab').forEach(tab => {

        tab.addEventListener(
            'click',
            () => {

                $$('.tab').forEach(item =>
                    item.classList.remove('active')
                );


                tab.classList.add('active');


                renderAdmin(
                    tab.dataset.filter
                );
            }
        );
    });
}


// --------------------------------------------------
// PAGE INITIALIZATION
// --------------------------------------------------

document.addEventListener(
    'DOMContentLoaded',
    () => {

        contactForm();

        testimonialForm();

        tabs();

        contactTabs();

        if ($('#testimonialCards')) {
            loadPublic();
        }

        if ($('#testimonialTableBody')) {
            loadAdmin();
        }

        if ($('#contactMessageTableBody')) {
            loadContactMessages();
        }
    }
);