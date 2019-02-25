import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { gql, graphql, compose } from 'react-apollo';
import Popup from 'react-popup';
import {notify} from 'react-notify-toast';
import Header from './Header';
import TableRow from './TableRow';
import AddSpeaker from './AddSpeaker';
import ShowSpeaker from './ViewSpeaker';

class ListSpeakers extends Component {

    constructor(props){
        super(props);
        this.state={
            newSpeaker: false,
            isEdit: false,
            speakerData: false,
            showSpeakerData: false,
            addLoader: false,
            editIndex: false,
            isOrderChanged: false,
            orderedSpeakers: [],
        };

    }
    componentDidMount() {
        /** Prompt plugin */
        Popup.registerPlugin('prompt', function (defaultValue, callback, speaker) {

            this.create({
                title: '',
                content: 'Are you sure want to delete this speaker?',
                buttons: {
                    left: ['cancel'],
                    right: [{
                        text: 'Delete',
                        className: 'success',
                        action: function () {
                            callback(speaker);
                            Popup.close();
                        }
                    }]
                }
            });
        });
    }


    render() {
        // console.log('all', this.props.data.allSpeakers);
        const { isOrderChanged, orderedSpeakers } = this.state;
        let speakers = this.props.data.allSpeakers;
        if(isOrderChanged){
            speakers = orderedSpeakers;
        }
        return (
            <div className="container">
                <Popup className="mm-popup"
                       btnClass="mm-popup__btn"
                       closeBtn={true}
                       closeHtml={null}
                       defaultOk="Ok"
                       defaultCancel="Cancel"
                       wildClasses={false}
                       closeOnOutsideClick={true} />
                <Header toggleSpeakeForm={this._showNewSpeakerForm} />
                <div className="row">
                <div className="col-sm-5">
                    { this.state.isOrderChanged && <div className='order-buttn'>
                        <button onClick={this._cancelSort}>Cancel</button>
                        <button onClick={this._updateOrder}>Update order</button>
                    </div> }
                <table className="table table-striped speakers-list table-bordered">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Name</th>
                            <th>Nachname</th>
                            <th>Live mode</th>
                            <th>Edit</th>
                            <th>Löschen</th>
                        </tr>
                    </thead>

                    { this.props.data.loading && <tbody><tr>
                        <td colSpan="8">Loading</td>
                    </tr></tbody>
                    }
                    <TableRow
                        speakers={speakers}
                        showSpeaker={this._showSpeaker}
                        editSpeaker={this._editSpeaker}
                        deleteSpeaker={this._deleteSpeaker}
                        orderChanged={this._orderChanged}
                    />
                </table>
                    </div>
                    <div className="col-sm-7">
                { this.state.newSpeaker && !this.state.showSpeakerData &&
                <AddSpeaker
                    showForm={this._showNewSpeakerForm}
                    isEdit={this.state.isEdit}
                    speaker = {this.state.speakerData}
                    addNewSpeaker = {this._saveSpeaker}
                    loader={this.state.addLoader}
                /> }
                { !this.state.newSpeaker && this.state.showSpeakerData &&
                    <ShowSpeaker
                        speaker = {this.state.speakerData}
                    />
                }
                    </div>
                </div>
            </div>
        );
    }

    static propTypes = {
        data: PropTypes.object,
    }
    _orderChanged = (isOrderChanged, data) => {
        // console.log("isOrderChanged", isOrderChanged, data);
        this.setState({isOrderChanged: true, orderedSpeakers: data});
    }
    _cancelSort = () => {
        this.setState({isOrderChanged: false});
    }
    _updateOrder = () => {
        this._updateSpeakerOrder(this.state.orderedSpeakers);
    }

    _showNewSpeakerForm = () => {
        this.setState({newSpeaker: true, isEdit: false, showSpeakerData: false, speakerData: false});
    }

    _showSpeaker = (speaker) => {
        // console.log('show speaker', speaker);
        this.setState({isEdit: false, newSpeaker: false, showSpeakerData: true, speakerData: speaker});
    }

    _editSpeaker = (speaker, key) => {
        // console.log('edit speaker', speaker);
        this.setState({isEdit: true, newSpeaker: true, showSpeakerData: false, speakerData: speaker, editIndex: key});
    }

    _deleteSpeaker = (speaker) => {
        /** Call the plugin */
        Popup.plugins().prompt('', this._confirmDelete, speaker);
    }

    _saveSpeaker =  (speaker) =>{
        speaker.isEdit ? this._updateSpeaker(speaker) : this._addSpeaker(speaker);
    }

    _addSpeaker = async (speaker) => {
        // console.log('speaker', speaker);
        const { name, surname, imageUrl, gender, description, publisher, logo, langs, topic, publications, audios, videos } = speaker;
        await this.props.CreateSpeakerMutation({
            variables: {
                name,
                surname,
                imageUrl,
                gender,
                description,
                publisher,
                logo,
                langs,
                topic,
                publications,
                audios,
                videos,
            }
        });
        setTimeout(() => {
            this.setState({addLoader: false});
            notify.show('Neuer Sprecher wurde hinzugefügt', 'success', 5000);
            this._showSpeaker(this.props.data.allSpeakers[0]);
        }, 2500);
    }

    _updateSpeaker = async (speaker) => {

        const { id, name, surname, imageUrl, gender, description, publisher, logo, langsIds, langs, topicIds, topic, publicationIds, publications,
            audioIds, audios, videoIds, videos } = speaker;
        console.log('topicIds', topicIds);
        console.log('topic', topic);
        const result = await this.props.UpdateSpeakerMutation({
            variables: {
                id,
                name,
                surname,
                imageUrl,
                gender,
                description,
                publisher,
                logo,
                langsIds,
                langs,
                topicIds,
                topic,
                publicationIds,
                publications,
                audioIds,
                audios,
                videoIds,
                videos,
            }
        });
        const index = this.state.editIndex;
        setTimeout(() => {
            this._showSpeaker(this.props.data.allSpeakers[index]);
            this.setState({addLoader: false, editIndex: false});
            notify.show('Die Speakerdaten wurden aktualisiert ', 'success', 5000);
        }, 2500);


    }

    _updateSpeakerOrder = async (speakers) => {
        const result = await speakers.map((speaker, index) => {
            const {id} = speaker;
            return  this.props.updateSpeakerOrder({
                variables: {
                    id,
                    order: index,
                }
            });
        });
        console.log('update', result);
        setTimeout(() => {
            this.setState({isOrderChanged: false});
            notify.show('Die Reihenfolge der Sprecher wurde aktualisiert', 'success', 5000);
        }, 2500);

    }
    _confirmDelete = async (speaker) => {
            // console.log('confirmed', speaker);
            const id = speaker.id;
            await this.props.deleteSpeakerMutation({
                variables: {
                    id,
                }
            });
    }

}
const DELETE_SPEAKER = gql `mutation 
    deleteSpeakerMutation($id: ID!){
    deleteSpeaker(id: $id){
    id
  }
}`

const UPDATE_SPEAKER_ORDER = gql `mutation
    updateSpeakerOrder($id: ID!, $order: Int) {
    updateSpeaker(
    id: $id,
    order: $order,
  ) {
    id
  }
  }
`
const UPDATE_SPEAKER = gql `mutation
    updateSpeakerMutation($id: ID!, $name: String!, $surname: String, $imageUrl: String, $gender: String, $description: String, $publisher: String, $logo: String, $langIds: [ID!], $langs: [SpeakerlanguagesLanguage!], $topicIds: [ID!], $topic: [SpeakertopicTopic!], 
    $publicationIds: [ID!], $publications: [SpeakerpublicationsesPublications!], $audioIds: [ID!], $audios: [SpeakeraudiosAudio!], $videoIds: [ID!], $videos: [SpeakervideosVideo!]) {
    updateSpeaker(
    id: $id,
    name: $name,
    surname: $surname,
    imageurl1: $imageUrl,
    gender: $gender,
    description: $description,
    publisher: $publisher,
    plogo: $logo,
    languages: $langs,
    languagesIds: $langIds,
    topic: $topic,
    topicIds: $topicIds,
    publicationsesIds: $publicationIds,
    publicationses: $publications,
    audiosIds: $audioIds,
    audios: $audios,
    videosIds: $videoIds,
    videos: $videos,
  ) {
    id
  }
  }
`
const CREATE_SPEAKER_MUTATION = gql`
  mutation CreateSpeakerMutation($name: String!, $surname: String, $imageUrl: String, $gender: String, $description: String, $publisher: String, $logo: String, $langs: [SpeakerlanguagesLanguage!]!, $topic: [SpeakertopicTopic!]!,
  $publications: [SpeakerpublicationsesPublications!], $audios: [SpeakeraudiosAudio!], $videos: [SpeakervideosVideo!]) {
    createSpeaker(
      name: $name,
      surname: $surname,
      imageurl1: $imageUrl,
      gender: $gender,
      description: $description,
      publisher: $publisher,
      plogo: $logo,
      languages: $langs,
      topic: $topic,
      publicationses: $publications,
      audios: $audios,
      videos: $videos,
    ) {
      id
    }
  }
`

const ALL_SPEAKERS = gql`query {
  allSpeakers(orderBy: order_ASC) {
      id
      audios {
          id
          name
          source
          url
      }
      videos {
          id
          name
          source
          url
      }    
      createdAt    
      gender
      description
      imageurl1
      languages{
          id
          english
          french
          german
          spanish
      }
      name
      publisher
      plogo
      order
      surname
      topic{
          id
          acting
          adventure
          bestseller
          bigdata
          comedy
          culture
          digital
          economy
          entertainment
          environment
          feminism
          figures
          yoga
          publications
          fiction
          pleasure
          politics
          art
          crime
      }
      publicationses{
          id
          name
          source
          url
      }
      updatedAt
  }
  _allSpeakersMeta {
      count
    }
}`


export default compose(graphql(ALL_SPEAKERS),
    graphql(DELETE_SPEAKER, { name: 'deleteSpeakerMutation', options: {refetchQueries:[{query:ALL_SPEAKERS}]} }),
    graphql(CREATE_SPEAKER_MUTATION, { name: 'CreateSpeakerMutation', options: {refetchQueries:[{query:ALL_SPEAKERS}]} }),
    graphql(UPDATE_SPEAKER, { name: 'UpdateSpeakerMutation', options: {refetchQueries:[{query:ALL_SPEAKERS}]} }),
    graphql(UPDATE_SPEAKER_ORDER, { name: 'updateSpeakerOrder', options: {refetchQueries:[{query:ALL_SPEAKERS}]} })
)(ListSpeakers);
